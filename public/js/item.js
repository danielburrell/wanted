var app = angular.module('wantedItem', [ 'wantedDialog', 'wantedDelete' ]);
app.directive('wantedItem', function() {
  return {
    restrict : 'A',
    scope : {
      steamId : '=steamId',
      mySteamId : '=mySteamId',
    },
    templateUrl : '/assets/templates/item.html',
    controller : [ '$scope', '$http', function($scope, $http) {
      $scope.addDetail = function() {
        url = '/api/addDetail/' + $scope.proposedDetail.wantedId;
        $http.post(url, $scope.proposedDetail).success(function(data) {
          console.log(data);
        }).error(function(data, status, headers, config) {
          // TODO: Error
          // handling for add
          // detail
        });
      }


      $scope.selectedState = [1,2];
      $scope.adjustFilter = function(filter) {
          filter.value = !filter.value;
          if (filter.value) {
            $scope.selectedState.push(filter.key);
          } else {
            $scope.currentPage = 0;
            var index = $scope.selectedState.indexOf(filter.key);
            $scope.selectedState.splice(index, 1);
          }
      }
      $scope.filter = {
        "obtained" : { "value" : false, "key" : 3 },
        "unobtained" : { "value" : true, "key" : 1 },
        "unwanted" : { "value" : false, "key" : 0 },
        "unknown" : { "value" : true, "key" : 2 },
      }

      $scope.proposedDetail = {
        "wantedId" : 1,
        "details" : {
          "quality" : -1,
          "levelNumber" : 1,
          "isTradable" : 1,
          "isCraftable" : 1,
          "craftNumber" : 0,
          "isGiftWrapped" : 0,
          "price" : "ASK",
          "isObtained" : 0,
          "priority" : undefined
        }
      };

      $scope.removeDetailFromModel = function(detailId) {
        for ( var i = 0; i < $scope.items.length; i++) {
          for ( var j = 0; j < $scope.items[i].details.length; j++) {
            if ($scope.items[i].details[j].detailId == detailId) {
              console.log("detailId Matches!");
              $scope.items[i].details.splice(j, 1);
            }
          }
        }
        ;
      };


      $scope.pushStateChangeToModel = function(wantedId, state) {
        console.log(wantedId + "vs" + state);
        for ( var i = 0; i < $scope.items.length; i++) {
          if ($scope.items[i].wantedId == wantedId) {
            console.log("wantedId Matches!");
            $scope.items[i].state = state;
          } else {
            console.log($scope.items[i].wantedId + " vs " + wantedId);
          }
        }
        ;
      };

      $scope.pushDetailToModel = function(wantedId, details) {
        console.log(wantedId + "vs" + details);
        for ( var i = 0; i < $scope.items.length; i++) {
          if ($scope.items[i].wantedId == wantedId) {
            console.log("wantedId Matches!");
            $scope.items[i].details.push(details);
          } else {
            console.log($scope.items[i].wantedId + " vs " + wantedId);
          }
        }
        ;
      };

      $scope.showAddDetail = function(wantedId) {
        $scope.proposedDetail.wantedId = wantedId;
      };

      $scope.showDeleteDetail = function(detailId) {
        $scope.proposedDetailDelete = detailId;
      };

      $scope.qualities = [ {
        "id" : "0",
        "name" : "Normal",
        "css" : "normal",
      }, {
        "id" : "1",
        "name" : "Genuine",
        "css" : "genuine",
      }, {
        "id" : "3",
        "name" : "Vintage",
        "css" : "vintage",
      }, {
        "id" : "5",
        "name" : "Unusual",
        "css" : "unusual",
      }, {
        "id" : "6",
        "name" : "Unique",
        "css" : "unique",
      }, {
        "id" : "7",
        "name" : "Community",
        "css" : "community",
      }, {
        "id" : "8",
        "name" : "Valve",
        "css" : "valve",
      }, {
        "id" : "9",
        "name" : "Self-Made",
        "css" : "selfmade",
      }, {
        "id" : "11",
        "name" : "Strange",
        "css" : "strange",
      }, {
        "id" : "13",
        "name" : "Haunted",
        "css" : "haunted",
      }, {
        "id" : "-1",
        "name" : "Any Quality",
        "css" : "default",
      } ];

      $scope.tradable = [ {
        "id" : "0",
        "name" : "Untradable"
      }, {
        "id" : "1",
        "name" : "Tradable"
      }, {
        "id" : "2",
        "name" : "Don't care"
      } ];

      $scope.tradableToString = function(tradable) {
        for ( var i = 0; i < ($scope.tradable).length; i++) {
          if ($scope.tradable[i].id == tradable) {
            return $scope.tradable[i].name;
          }
        }
      };



      $scope.markAs = function(wantedId, state) {
        url = '/api/markAs/' + wantedId + '/'+ state;
        $http.post(url).success(function(data) {
          console.log(data);
          console.log(wantedId);
          console.log(state);
          $scope.pushStateChangeToModel(wantedId,
            state
          );
        }).error(function(data, status, headers, config) {
          // TODO: Error
          // handling for add
          // detail
        });
      }
      $scope.craftable = [ {
        "id" : "0",
        "name" : "Uncraftable"
      }, {
        "id" : "1",
        "name" : "Craftable"
      }, {
        "id" : "2",
        "name" : "Don't care"
      } ];

      $scope.craftableToString = function(craftable) {
        for ( var i = 0; i < ($scope.craftable).length; i++) {
          if ($scope.craftable[i].id == craftable) {
            return $scope.craftable[i].name;
          }
        }
      };

      $scope.giftwrapped = [ {
        "id" : "0",
        "name" : "Not Wrapped"
      }, {
        "id" : "1",
        "name" : "Wrapped"
      }, {
        "id" : "2",
        "name" : "Don't care"
      } ];

      $scope.giftwrapToString = function(giftwrapped) {
        for ( var i = 0; i < ($scope.giftwrapped).length; i++) {
          if ($scope.giftwrapped[i].id == giftwrapped) {
            return $scope.giftwrapped[i].name;
          }
        }
      };

      $scope.craftNumberToString = function(craftNumber) {
        if (craftNumber == -1) {
          return "Any";
        } else if (craftNumber == 0) {
          return "Without CraftNumber";
        } else {
          return "Specific CraftNumber";
        }
        ;
      }

      $scope.qualityIdToString = function(inputId) {
        for ( var i = 0; i < ($scope.qualities).length; i++) {
          if ($scope.qualities[i].id == inputId) {
            return $scope.qualities[i].name;
          }
        }
      };

      $scope.currentPage = 0;
      $scope.pageSize = 10;
      $scope.numberOfPages = function() {
        return Math.ceil($scope.items.length / $scope.pageSize);
      }

      $scope.previous = function(currentPage) {
        if (currentPage > 0) {
          return $scope.currentPage--;
        } else {
          return $scope.currentPage;
        }
      };

      $scope.next = function(currentPage) {
        if (currentPage < $scope.numberOfPages()) {
          return $scope.currentPage++;
        } else {
          return $scope.currentPage;
        }
      };

      $scope.getWanted = function(steamId) {

        url = '/api/getWantedList/' + steamId;
        console.log(url);
        $http.get(url).success(function(data) {
          $scope.items = data.item;
        });
      };

    } ],

    link : function(scope, iElement, iAttrs) {
      // get weather details
      scope.getWanted(scope.steamId);
    },
  }
});
app.filter('startFrom', function() {
  return function(input, start) {
    start = +start; // parse to int
    return input.slice(start);
  }
});
app.filter('stateFilter', [ function() {
  return function(items, selectedState) {
    if (!angular.isUndefined(items) && !angular.isUndefined(selectedState) && selectedState.length > 0) {
      var tempItems = [];
      angular.forEach(selectedState, function(state) {
        angular.forEach(items, function(item) {
          if (angular.equals(item.state, state)) {
            tempItems.push(item);
          }
        });
      });
      return tempItems;
    } else {
      return items;
    }
  };
} ])
