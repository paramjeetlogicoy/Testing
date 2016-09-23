var inventoryRouter = function($routeProvider) {
	
	$routeProvider.
	
	when('/products/add-products', {
		templateUrl: '/resources/ng-templates/admin/inv/views/add-products.html'+versionCtrl,
		controller: 'addProductsCtrl'
	}).
  
	when('/products/add-categories', {
		templateUrl: '/resources/ng-templates/admin/inv/views/add-categories.html'+versionCtrl,
		controller: 'addCategoryCtrl'
    }).
    
    when('/products/add-strains', {
         templateUrl: '/resources/ng-templates/admin/inv/views/add-strains.html'+versionCtrl,
         controller: 'addStrainsCtrl'
    }).
  
    when('/add-people', {
    	templateUrl: '/resources/ng-templates/admin/inv/views/add-people.html'+versionCtrl,
    	controller: 'addPplCtrl'
    }).
  
  	when('/purchases', {
         templateUrl: '/resources/ng-templates/admin/inv/views/purchases.html'+versionCtrl,
         controller: 'addPurchaseCtrl'
       /* 	 ,
         resolve : {
        	 currentUser : globalResolve.currentUser
        }*/
    }).
    
    when('/packets', {
        templateUrl: '/resources/ng-templates/admin/inv/views/packets.html'+versionCtrl,
        controller: 'addPacketCtrl'
    }).
    
    
    when('/inventory/box-inventory', {
        templateUrl: '/resources/ng-templates/admin/inv/views/box-inventory.html'+versionCtrl,
        controller: 'addBoxInvCtrl'
    }).
    
    when('/inventory/shop-inventory', {
        templateUrl: '/resources/ng-templates/admin/inv/views/shop-inventory.html'+versionCtrl,
        controller: 'shopInvCtrl'
    }).
    
    when('/settings/logs', {
        templateUrl: '/resources/ng-templates/admin/inv/views/logs.html'+versionCtrl,
        controller: 'logCtrl'
    }).
    
    when('/settings/returns', {
        templateUrl: '/resources/ng-templates/admin/inv/views/returns.html'+versionCtrl,
        controller: 'returnsCtrl'
    }).
    
    when('/settings/bulk-shop-assign', {
        templateUrl: '/resources/ng-templates/admin/inv/views/bulk-assign.html'+versionCtrl,
        controller: 'bulkAssignCtrl'
    }).
    
    when('/settings/barcodes', {
        templateUrl: '/resources/ng-templates/admin/inv/views/generate-barcode.html'+versionCtrl,
        controller: 'barCodeCtrl'
    }).
    
    when('/settings/stats', {
        templateUrl: '/resources/ng-templates/admin/inv/views/stats.html'+versionCtrl,
        controller: 'statsCtrl'
    }).
    
    when('/inventory/inv-alert-settings', {
         templateUrl: '/resources/ng-templates/admin/inv/views/inv-alerts.html'+versionCtrl,
         controller: 'invAlertCtrl'
    }).
    
    when('/inventory/take-stock', {
         templateUrl: '/resources/ng-templates/admin/inv/views/stock-taking.html'+versionCtrl,
         controller: 'stockTakeCtrl'
    }).
    
    when('/inventory/misc', {
         templateUrl: '/resources/ng-templates/admin/inv/views/misc-report.html'+versionCtrl,
         controller: 'miscReportCtrl'
    }).
    
  	otherwise({
        redirectTo: '/purchases'
    });
};