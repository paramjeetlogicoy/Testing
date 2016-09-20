var shopInvCtrlr = function($scope, $http, $rootScope, $filter, currentUser){

	$rootScope.rootPage = "Shop Inventory";
	$scope.controlId = currentUser.ctrlid;
	
	$scope.sis = [];
	$scope.grantTotal = 0;
	
	$scope.sortType = "productId";
	$scope.sortReverse = false;
	$scope.productSearch = "";
	$scope.dateFilter = new Date();
	$scope.loading = true;
	
	$scope.loadData = function(){
		$http.get('/inventory/apps/listshopinv?d='+$filter('date')($scope.dateFilter, 'MM/dd/yyyy')
				+ '&sp=' + $scope.shopSelected.id).success(function(data){
			if(data.success){
				$scope.sis = data.result;
				$scope.getTotal();
				
				$scope.loading = false;
			}
		});		
	};
	
	$scope.getTotal=function(){
	    $scope.grantTotal = 0;
	    for(var i = 0; i < $scope.sis.length; i++){
	    	$scope.grantTotal += $scope.sis[i].inventoryCost;
	    }	
	};
	
	$scope.setTotal=function(){
	    var total = 0;
	    $('.invcost').each(function(){
	    	total += $(this).data('invcost');
	    });
	    
	    $scope.grantTotal = total;
	};
	
	
	$scope.reload=function(){
		$scope.loading = true;
		$scope.sis = [];
		$scope.loadData();
	};
	
	$scope.getLowInventoryCSV = function(){
		window.location = '/inventory/apps/lowinventorycsv?iacsv=1';
	};
	
	$scope.shopSelected = {id:'-1',shopName:'All Shops'};
	$scope.shops = [];
	$http.get('/inventory/apps/listshops').success(function(data){
		if(data.success){
			$scope.shops = data.result;
			$scope.shops.unshift($scope.shopSelected);
			
			//initial load
			$scope.loadData();
		}
	});
};