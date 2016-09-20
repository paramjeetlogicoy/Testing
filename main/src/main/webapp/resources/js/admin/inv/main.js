//Create a random number for version control and avoid caching
var  _luvbriteGlobalOpsId = 0,
versionCtrl = "?v" + Math.random(),
_globalPmtModes = [{label:'Cash',value:'Cash'},{label:'Credit Card',value:'Credit Card'},{label:'Paypal',value:'Paypal'},{label:'Split',value:'Split'}],

sortColFns = function($scope, param, e){
	$scope.sortBy = param;
	var $currentTarget = $(e.currentTarget);
	
	/** remove sort class from all elements and later add it
	 ** just to the required one. 
	 **/
	if($currentTarget.hasClass('sdir-asc')){
		$scope.sdir='DESC';
		$('.sdir-asc,.sdir-desc').removeClass('sdir-desc sdir-asc');
		$currentTarget.addClass('sdir-desc');
	}
	else {
		$scope.sdir='ASC';
		$('.sdir-asc,.sdir-desc').removeClass('sdir-desc sdir-asc');
		$currentTarget.addClass('sdir-asc');
	}	
	
	$scope.reload();			
};

var app = angular.module(
	'invApp', [
         'ngRoute',
         'ngSanitize',
         'ui.bootstrap'
     ]
),


globalResolve = {
	
	currentUser : function($q, $rootScope, $http){
		var deferred = $q.defer();
		
		if($rootScope.globals && $rootScope.globals.currentUser){
			_luvbriteGlobalOpsId = $rootScope.globals.currentUser.ctrlid;
			deferred.resolve($rootScope.globals.currentUser);
		}
		else{
			
			$http.get('/inventory/apps/getopid', {params : { 'mid' : _globalUserId}}).success(function(data){
				if(data.success){
					_luvbriteGlobalOpsId = data.op.id;

					$rootScope.globals = {
			                currentUser: {
			                    username: data.op.userName,
			                    opsname : data.op.operatorName,
			                    ctrlid : data.op.id
			                }
			            };
					
					deferred.resolve($rootScope.globals.currentUser);
				}
				else{
					deferred.reject();
				}
			});
		}
		
		return deferred.promise;
	}
};




/**
 * All the controller functions are implemented here. 
 * Scroll to the bottom to see it being attached to the App.
 */ 
var 
defaultCtrlr = function($scope, $http, $filter, $sanitize){
};

app.config(['$routeProvider',
    function($routeProvider) {
      $routeProvider.
      	when('/purchases', {
	         templateUrl: '/resources/ng-templates/admin/inv/views/purchases.html'+versionCtrl,
	         controller: 'addPurchaseCtrl',
	         resolve : {
	        	 currentUser : globalResolve.currentUser
	        }
	    }).
        
        when('/packets', {
            templateUrl: '/resources/ng-templates/admin/inv/views/packets.html'+versionCtrl,
            controller: 'addPacketCtrl',
	         resolve : {
	        	 driverMode:function () {
	        		 return false;
	        	 },
	        	 currentUser : globalResolve.currentUser
	         }
	    }).
	    
	    when('/settings/stats', {
	         templateUrl: '/resources/ng-templates/admin/inv/views/stats.html'+versionCtrl,
	         controller: 'statsCtrl',
	         resolve : {
	        	 currentUser : globalResolve.currentUser
	        }
	    }).
	    
	    when('', {
	         templateUrl: '/resources/ng-templates/empty.html',
	         controller: 'defaultRouteCtrl'
	    });
}])



//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js

.run(function ($rootScope, $location, $http) {
    
    $rootScope.$on('$locationChangeStart', function (event, next, current) {
    	
    	//if the current page is /inv-alert-settings, then check if there is any unsaved changes
    	if(current.indexOf('inv-alert-settings')>-1 && $('.inv-save-alert>.glyphicon:visible').size()>0){
    	    
    		var sure = confirm('You have unsaved changes. Leave the page?');
    		if(!sure){
            	event.preventDefault();
                return;        			
    		}        		
    	}

        /**
         * Highlight the active menu 
         **/
        $('ul.navbar-nav a').removeClass('active').each(function(){
        	var $this = $(this),
        		href = $this.attr('href');
        	if(href && href.length>1 && ('#'+$location.path().substr(0, href.length)==href)){
        		$this.addClass('active');
        		
        		if($this.closest('ul').hasClass('dropdown-menu')){
        			$this.closest('ul').prev('a').addClass('active');
        		}
        		
        		return false;
        	}
        });
        
        //toggle the menu back
        $('button.navbar-toggle').not('.collapsed').click();
    });
})

.filter('abs', function () {
  return function(val) {
    return Math.abs(val);
  }
})

.directive('editform', function($rootScope) {
	  return {
	    templateUrl: function(elem, attr){
	    	if($rootScope.globals && $rootScope.globals.currentUser && $rootScope.globals.currentUser.ctrlid <= attr.limit)
	    		return '/resources/ng-templates/admin/inv/forms/'+attr.type+'.html'+versionCtrl;
	    	else
	    		return '/resources/ng-templates/empty.html';
	    }
	  };
})

.controller('defaultRouteCtrl', defaultCtrlr)
.controller('addPurchaseCtrl', addPurchaseCtrlr)
.controller('addPacketCtrl', addPacketCtrlr)
.controller('ModalSalesInfoCtrl', ModalSalesInfoCtrlr)
.controller('statsCtrl', statsCtrlr);

