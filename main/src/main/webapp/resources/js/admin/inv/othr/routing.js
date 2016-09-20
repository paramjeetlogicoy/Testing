var inventoryRouter = function($routeProvider) {
	
	$routeProvider.
	
	when('/products/add-products', {
		templateUrl: '/resources/ng-templates/admin/inv/views/add-products.html'+versionCtrl,
		controller: 'addProductsCtrl',
        resolve : {
        	currentUser : globalResolve.currentUser
        }
	}).
  
	when('/products/add-categories', {
		templateUrl: '/resources/ng-templates/admin/inv/views/add-categories.html'+versionCtrl,
		controller: 'addCategoryCtrl',
        resolve : {
        	currentUser : globalResolve.currentUser
        }
    }).
    
    when('/products/add-strains', {
         templateUrl: '/resources/ng-templates/admin/inv/views/add-strains.html'+versionCtrl,
         controller: 'addStrainsCtrl',
	        resolve : {
	        	currentUser : globalResolve.currentUser
	        }
    }).
  
    when('/add-people', {
    	templateUrl: '/resources/ng-templates/admin/inv/views/add-people.html'+versionCtrl,
    	controller: 'addPplCtrl',
        resolve : {
        	currentUser : globalResolve.currentUser
        }
    }).
  
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
    
    
    when('/inventory/box-inventory', {
        templateUrl: '/resources/ng-templates/admin/inv/views/box-inventory.html'+versionCtrl,
        controller: 'addBoxInvCtrl',
        resolve : {
        	 currentUser : globalResolve.currentUser
        }
    }).
    
    when('/inventory/shop-inventory', {
        templateUrl: '/resources/ng-templates/admin/inv/views/shop-inventory.html'+versionCtrl,
        controller: 'shopInvCtrl',
        resolve : {
        	currentUser : globalResolve.currentUser
        }
    }).
    
    when('/settings/logs', {
        templateUrl: '/resources/ng-templates/admin/inv/views/logs.html'+versionCtrl,
        controller: 'logCtrl',
        resolve : {
        	 currentUser : globalResolve.currentUser
        }
    }).
    
    when('/settings/returns', {
        templateUrl: '/resources/ng-templates/admin/inv/views/returns.html'+versionCtrl,
        controller: 'returnsCtrl',
        resolve : {
        	 currentUser : globalResolve.currentUser
        }
    }).
    
    when('/settings/bulk-shop-assign', {
        templateUrl: '/resources/ng-templates/admin/inv/views/bulk-assign.html'+versionCtrl,
        controller: 'bulkAssignCtrl',
        resolve : {
        	 currentUser : globalResolve.currentUser
        }
    }).
    
    when('/settings/barcodes', {
        templateUrl: '/resources/ng-templates/admin/inv/views/generate-barcode.html'+versionCtrl,
        controller: 'barCodeCtrl',
        resolve : {
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
    
    when('/inventory/inv-alert-settings', {
         templateUrl: '/resources/ng-templates/admin/inv/views/inv-alerts.html'+versionCtrl,
         controller: 'invAlertCtrl',
         resolve : {
         	currentUser : globalResolve.currentUser
         }
    }).
    
    when('/inventory/take-stock', {
         templateUrl: '/resources/ng-templates/admin/inv/views/stock-taking.html'+versionCtrl,
         controller: 'stockTakeCtrl',
         resolve : {
         	currentUser : globalResolve.currentUser
         }
    }).
    
    when('/inventory/misc', {
         templateUrl: '/resources/ng-templates/admin/inv/views/misc-report.html'+versionCtrl,
         controller: 'miscReportCtrl',
         resolve : {
         	currentUser : globalResolve.currentUser
         }
    }).
    
  	otherwise({
        redirectTo: '/purchases'
    });
};