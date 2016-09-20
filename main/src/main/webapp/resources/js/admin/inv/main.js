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
defaultCtrlr = function(){};

app

// function defined in routing.js
.config(['$routeProvider', inventoryRouter])

//below fns are defined in angular-general-functions.js
.config(['$httpProvider', appConfigs])
.run(['$http', '$rootScope', '$interval', appRunFns]) 
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
    
    
    /**Print Barcode Control**/
    $rootScope.addToBCQueue = function(newBcs){
    	var cq = {};
		cq.date = new Date();
		cq.printed = false;
		cq.bcs = [];
    	
    	if(!$rootScope.aq.length) {
    		$rootScope.aq.push(cq);
    	}
    	
    	var cqExists = false;
    	for(var i=0;i<$rootScope.aq.length; i++){
    		if(!$rootScope.aq[i].printed){
    			cq = $rootScope.aq[i];
    			cqExists = true;
    			break;
    		}
    	}
    	
    	if(!cqExists){
    		$rootScope.aq.unshift(cq);
    	}
    	
		newBcs.forEach(function(x){
			cq.bcs.push(x);
		});

		//Save it back to localStorage
		localStorage.setItem('lbBarcodeInfo', JSON.stringify($rootScope.aq));
		$rootScope.toggleView = true;
    };
    
    $rootScope.printFromBCQueue = function(){
    	var skipCols = 0,
    	cq = this.q;
    	if(this.pblSkipRows){
    		skipCols += (this.pblSkipRows)*4;
    	}
    	if(this.pblSkipCols){
    		skipCols += this.pblSkipCols;
    	}
    	
    	if(cq.bcs && cq.bcs.length>0){
    		cq.date = new Date();
    		cq.printed = true;
    		
    		//Save it back to localStorage
    		localStorage.setItem('lbBarcodeInfo', JSON.stringify($rootScope.aq));
    		$rootScope.toggleView = false;
    		
    		window.location = 'apps/getlabelsku?codes=' 
				+ encodeURIComponent(cq.bcs)
				+ '&s=' + skipCols;
    	}
    };
    
    $rootScope.pblSkipRows = 0;
    $rootScope.pblSkipCols = 0;
    $rootScope.toggleView = false;
    
    $rootScope.aq = [];
    $rootScope.cq = {};
    var initBarcodeCtrl = function(){
    	var barcodeInfo = JSON.parse(localStorage.getItem("lbBarcodeInfo"));
    	if(barcodeInfo && barcodeInfo.length>0){
    		
    		if(barcodeInfo.length>=4){
    			$rootScope.aq = barcodeInfo.splice(0,4);
    		}
    		
    		else{
    			$rootScope.aq = barcodeInfo;
    		}
    	}
    };
    initBarcodeCtrl();
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
.controller('addPurchaseCtrl', ['$scope', '$http', '$filter', '$rootScope', '$uibModal', 'currentUser', addPurchaseCtrlr])
.controller('addPacketCtrl', ['$scope', '$http', '$routeParams', 'driverMode', '$uibModal', '$rootScope', 'currentUser', addPacketCtrlr])
.controller('addPplCtrl', ['$scope', '$http', '$filter', '$rootScope', '$compile', 'currentUser', addPplCtrlr])
.controller('addProductsCtrl', ['$scope', '$http', '$rootScope', '$filter', 'currentUser', addProductsCtrlr])
.controller('addStrainsCtrl', ['$scope', '$http', '$filter', '$rootScope', 'currentUser', addStrainsCtrlr])
.controller('addCategoryCtrl', ['$scope', '$http', '$filter', '$rootScope', 'currentUser', addCategoryCtrlr])
.controller('addBoxInvCtrl', ['$scope', '$http', '$routeParams', 'currentUser', addBoxInvCtrlr])
.controller('shopInvCtrl', ['$scope', '$http', '$rootScope', '$filter', 'currentUser', shopInvCtrlr])
.controller('statsCtrl', ['$scope', '$http', '$filter', '$uibModal', '$rootScope', 'currentUser', statsCtrlr])
.controller('returnsCtrl',['$scope', '$http', '$rootScope', 'currentUser', returnsCtrlr])
.controller('bulkAssignCtrl',['$scope', '$http', '$rootScope', 'currentUser', bulkAssignCtrlr])
.controller('barCodeCtrl',['$scope', '$http', '$rootScope', 'currentUser', barCodeCtrlr])
.controller('logCtrl',['$scope', '$http', '$rootScope', 'currentUser', logCtrlr])
.controller('invAlertCtrl',['$scope', '$http', '$rootScope', 'currentUser', invAlertCtrlr])
.controller('miscReportCtrl',['$scope', '$http', '$rootScope', 'currentUser', miscReportCtrlr])
.controller('stockTakeCtrl',['$scope', '$http', '$rootScope', '$timeout', 'currentUser', stockTakeCtrlr])

.controller('ModalSalesInfoCtrl', ['$scope', '$uibModalInstance', 'modalScope', '$http', '$filter', '$rootScope', ModalSalesInfoCtrlr]);

