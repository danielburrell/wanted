package controllers;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.Detail;
import models.Item;
import models.SteamUser;

import org.expressme.openid.Association;
import org.expressme.openid.Endpoint;
import org.expressme.openid.OpenIdManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import play.Logger;
import play.db.DB;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.OpenID;
import play.libs.OpenID.UserInfo;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import uk.co.solong.tf2.schema.Items;
import views.html.index;

import com.fasterxml.jackson.databind.JsonNode;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render(""));
    }

    public static Result privacy() {
        return TODO;
    }

    public static Result about() {
        return TODO;
    }

    public static Result wanted(Long steamId) {
        return ok(index.render(""));
    }

    public static Result logout() {
        session().clear();
        return ok();
    }

    public static Result loginStatus() {
        String steamId = session("steamId");
        Map<String, Object> map = new HashMap<String, Object>();

        if (steamId != null) {
            map.put("steamId", steamId);
            map.put("loggedIn", true);
            JsonNode result = Json.toJson(map);
            return ok(result);
        } else {
            map.put("loggedIn", false);
            JsonNode result = Json.toJson(map);
            return ok(result);
        }
    }

    public static Promise<Result> openIDCallback() {
        Promise<UserInfo> d = OpenID.verifiedId();

        return d.map(new Function<UserInfo, Result>() {
            public Result apply(UserInfo userInfo) {
                Long steamId = getSteamIdFromResponseUrl(userInfo.id);
                if (steamId > 0) {
                    // perform login tasks
                    session("steamId", steamId.toString());
                    // call newUserItemNothing function.
                    int userStatus = getUserWelcomeStatus(steamId);
                    switch (userStatus) {
                    case 0:
                        return ok(index.render(""));

                    case 1:
                        return ok(index.render(""));//"Oh hey, you're back! New items have been released since you were last here. Go! Go! Go!"

                    case 2:
                        return ok(index.render("")); //"Oh hey, you're back! No new items have been released since you were last here."

                    default:
                        return ok("What is this I don't even..");
                    }

                } else {
                    return ok("Doesn't look like logging in worked. Maybe try again?");
                }

            }

            /**
             * Calls out to the database to obtain the UserWelcomeStatus:
             *
             * @param steamId
             *
             * @return 0 if the user is new
             * @return 1 if the user is not new, but there are new items
             * @return 2 if the user is not new and there are no new items
             */
            public int getUserWelcomeStatus(Long steamId) {
                // TODO Auto-generated method stub
                Logger.info("Checking welcome status");
                Object[] params = new Object[] { steamId };
                JdbcTemplate jdbcTemplate = new JdbcTemplate(DB.getDataSource());
                SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate).withSchemaName("wanted").withProcedureName("getWelcomeStatus");
                Map<String, Object> result = call.execute(params);
                return (int) result.get("welcome_status");
            }

            private Long getSteamIdFromResponseUrl(String responseUrl) {
                String[] steamIdFragments = responseUrl.split("/");
                try {
                    return Long.parseLong(steamIdFragments[5]);
                } catch (Throwable nfe) {
                    return null;
                }
            }
        });

    }

    public static Result login() {

        try {
            OpenIdManager manager = new OpenIdManager();
            manager.setReturnTo("http://localhost:8010/openIDCallback");
            manager.setRealm("http://localhost:8010/");

            Endpoint endpoint = manager.lookupEndpoint("http://steamcommunity.com/openid");
            Association association = manager.lookupAssociation(endpoint);
            String url = manager.getAuthenticationUrl(endpoint, association);
            return redirect(url);
        } catch (Exception e) {
            return ok("Steam community is down :(");
        }

        /*
         *
         * Map<String, String> attributes = new HashMap<String, String>();
         * attributes.put("email", "http://schema.openid.net/contact/email");
         * Promise<String> p =
         * OpenID.redirectURL("http://steamcommunity.com/openid",
         * "http://localhost:8010/openIDCallback", null, null,
         * "http://localhost:8010/");
         *
         * return p.map(new Function<String, Result>() { public Result
         * apply(String url) { return redirect(url); } });
         */
    }

    public static Result getWantedList(Long steamId) {
        // call the dao to get the wanted list from the database
        Logger.info("Getting Wanted List For {}", steamId);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DB.getDataSource());
        Object[] params = new Object[] { steamId };
        List<Map<String, Object>> results = jdbcTemplate.queryForList("CALL `wanted`.`getWantedList`(?)", params);
        SteamUser s = new SteamUser();
        s.steamId = steamId;
        s.item = new LinkedList<Item>();
        Long currentWantedId = null;
        Item currentItem = null;
        for (Map<String, Object> row : results) {
            Long wantedId = (Long) row.get("wanted_id");
            if (wantedId != currentWantedId) {
                Item item = new Item();
                item.wantedId = wantedId;
                item.itemId = (Long) row.get("item_id");
                item.state = (Integer) row.get("state");
                item.details = new LinkedList<Detail>();
                currentWantedId = wantedId;
                s.item.add(item);
                currentItem = item;
            }

            Long detailId = (Long) row.get("detail_id");
            if (detailId != null) {
                Detail detail = new Detail();
                detail.detailId = detailId;
                detail.craftNumber = (Long) row.get("craft_number");
                detail.isCraftable = (Boolean) row.get("is_craftable");
                detail.isGiftWrapped = (Boolean) row.get("is_gift_wrapped");
                detail.isObtained = (Boolean) row.get("is_obtained");
                detail.isTradable = (Boolean) row.get("is_tradable");
                detail.levelNumber = (Integer) row.get("level_number");
                detail.price = (String) row.get("price");
                detail.quality = (Integer) row.get("quality");
                currentItem.details.add(detail);
            }

        }

        JsonNode result = Json.toJson(s);
        return ok(result);

        // JsonNode result = Json.toJson(user);
        /*
         * DataSource ds = DB.getDataSource(); JdbcTemplate d = new
         * JdbcTemplate(ds); d.qu s.setSql("CALL `wanted`.`getWantedList`(?)");
         * s.setJdbcTemplate(d); s.qu(steamId);
         *
         *
         * if (steamId == 4027L) { ArrayList<Item> wantedList = new
         * ArrayList<Item>(); Item item = new Item(); item.itemId = 5000L;
         * ArrayList<Detail> details = new ArrayList<Detail>(); Detail detail =
         * new Detail(); detail.craftNumber = 666L; detail.detailId = 11L;
         * detail.isCraftable = true; detail.isGiftWrapped = true;
         * detail.isObtained = true; detail.isTradable = true; detail.level =
         * 100; detail.quality = 3; detail.price = "3 Keys";
         * details.add(detail); item.details = details; wantedList.add(item);
         * wantedList.add(item); wantedList.add(item);
         *
         * JsonNode result = Json.toJson(wantedList); return ok(result); } else
         */
        // return TODO;
    }

    public static Result wantedPartial() {
        return TODO;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result addDetail(Long wantedId) {
        JsonNode json = request().body().asJson();// (Detail.class);
        Item item = Json.fromJson(json, Item.class);
        Detail detail = item.details.get(0);

        System.out.println(request().body());

        long steamId = Long.parseLong(session("steamId"));

        try {
            CallableStatement cs = DB.getDataSource().getConnection().prepareCall("CALL `wanted`.`addDetail`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            cs.setLong(1, steamId);
            cs.setLong(2, item.wantedId);
            cs.setInt(3, detail.quality);
            cs.setInt(4, detail.levelNumber);
            cs.setBoolean(5, detail.isTradable);
            cs.setBoolean(6, detail.isCraftable);
            cs.setLong(7, detail.craftNumber);
            cs.setBoolean(8, detail.isGiftWrapped);
            cs.setBoolean(9, detail.isObtained);
            cs.setInt(10, detail.priority);
            cs.setString(11, detail.price);

            cs.execute();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // (<{steamId bigint}>, <{wantedId bigint}>, <{quality int}>, <{level
        // smallint}>, <{isTradable tinyint}>, <{isCraftable tinyint}>,
        // <{craftNumber bigint}>, <{isGiftWrapped tinyint}>, <{isObtained
        // tinyInt}>, <{priority int}>, <{price varchar(45)}>

        // user.save();
        /*
         * List<Detail> details = new ArrayList<Detail>(1); details.add(detail);
         * Item item = new Item(); item.wantedId = wantedId; item.details =
         * details; SteamUser user = new SteamUser(); user.steamId =
         * Long.parseLong(session("steamId"));
         *
         * JsonNode result = Json.toJson(1);
         */
        return TODO;
    }

    public static Result editDetail(Long detailId) {
        // requires index on [detailId & steamId]
        // update wanted.details () where detailId = x and steamId = session()

        return TODO;
    }

    public static Result deleteDetail(Long detailId) {
        // require index on [detailId & steamId]
        // delete from wanted.details where detailId = x and steamId = session()

        return TODO;
    }

    public static Result markAsObtained(Long detailId) {
        // requires index on [detailId & steamId]
        // update wanted.details () where detailId = x and steamId = session()
        // set obtained = 1

        return TODO;
    }

    public static Result markAsUnobtained(Long detailId) {
        // requires index on [detailId & steamId]
        // update wanted.details () where detailId = x and steamId = session()
        // set obtained = 0
        return TODO;
    }
}
