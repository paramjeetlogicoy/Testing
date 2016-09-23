var addProductsCtrlr = function($scope, $http, $rootScope, $filter){
	
	$rootScope.rootPage = "Products";
	$scope.controlId = _luvbriteGlobalOpsId;
	
	$scope.productBtnText = 'ADD';
	$scope.currentProductId = 0;
	$scope.categories = [];
	$scope.filterCategories = [];
	$scope.categoryFilterSelected = {"id":0,"categoryName":"All"};
	$http.get('/inventory/apps/listcats').success(function(data){
		if(data.success){
			$scope.categories = data.result;
			$scope.categorySelected = $scope.categories[0];			
	
			$scope.filterCategories = $scope.categories.slice();
			$scope.filterCategories.unshift({"id":0,"categoryName":"All"});
			$scope.categoryFilterSelected = $scope.filterCategories[0];
		}
	});
	
	$scope.strains = [];
	$scope.filterStrains = [];
	$scope.strainFilterSelected = {"id":0,"strainName":"All"};
	$http.get('/inventory/apps/liststrains').success(function(data){
		if(data.success){
			$scope.strains = data.result;
			$scope.strainSelected = $scope.strains[0];
			
			$scope.filterStrains = $scope.strains.slice();
			$scope.filterStrains.unshift({"id":0,"strainName":"All"});
			$scope.strainFilterSelected = $scope.filterStrains[0];
		}
	});
	
	$scope.addProduct=function(){
		
		if($scope.currentProductId!=0){			
			for(var i=0;i<$scope.products.length;i++){
				if($scope.products[i].id==$scope.currentProductId){					
					$scope.products[i].productName = $scope.productName;
					$scope.products[i].nickName = $scope.nickName;
					
					$scope.products[i].categoryId = $scope.categorySelected.id;
					$scope.products[i].categoryName = $scope.categorySelected.categoryName;
					
					$scope.products[i].strainId = $scope.strainSelected.id;
					$scope.products[i].strainName = $scope.strainSelected.strainName;
	
					break;
				}
			}
			
			$.post('/inventory/apps/updateproduct',{id:$scope.currentProductId, pn:$scope.productName, 
						nn:$scope.nickName, ci:$scope.categorySelected.id,
						si:$scope.strainSelected.id, opsid:_luvbriteGlobalOpsId},
				function(data){
					if(!data.success) alert(data.message);
				}
			);			
			
			$scope.currentProductId = 0;
			$scope.productName = '';
			$scope.nickName = '';
			
			$scope.productBtnText = 'ADD';
		}
		else {
			
			$scope.products.push({
				productName:$scope.productName, 
				nickName:$scope.nickName, 
				
				categoryName:$scope.categorySelected.categoryName, 
				categoryId:$scope.categorySelected.id, 
				
				strainName:$scope.strainSelected.strainName, 
				strainId:$scope.strainSelected.id, 
				
				formattedDateAdded: $filter('date')(new Date(), 'MM/dd/yyyy hh:mm a')
			});
			
			$.post('/inventory/apps/newproduct',{pn:$scope.productName, 
						nn:$scope.nickName, ci:$scope.categorySelected.id,
						si:$scope.strainSelected.id, opsid:_luvbriteGlobalOpsId},
						
				function(data){
					if(data.success){
						for(var i=0;i<$scope.products.length;i++){
							if($scope.products[i].productName==data.prod.productName){
								$scope.products[i].formattedDateAdded = data.prod.formattedDateAdded;
								$scope.products[i].id = data.prod.id;
								try{$scope.$apply();}catch(err){}
								break;
							}
						}
					}
					else{
						alert(data.message);
					}
				}
			);
			
	
			$scope.productName = '';
			$scope.nickName = '';
		}
		
	};
	
	$scope.editProduct=function($event){
		$event.preventDefault();		
		
		$scope.productName = this.product.productName;
		$scope.nickName = this.product.nickName;
		
		for(var i=0;i<$scope.categories.length;i++){
			if($scope.categories[i].id==this.product.categoryId){
				$scope.categorySelected = $scope.categories[i];
				break;
			}
		}
		
		for(var i=0;i<$scope.strains.length;i++){
			if($scope.strains[i].id==this.product.strainId){
				$scope.strainSelected = $scope.strains[i];
				break;
			}
		}
		
		$scope.currentProductId = this.product.id;
		
		$scope.productBtnText = 'SAVE';
		
		/**
		 * Scroll up to the form and show subtle color change.
		 **/
		_lbFns.showViewForm();
	};
	
	$scope.cancelEdit=function($event){
		$event.preventDefault();
		
		$scope.currentProductId = 0;
		$scope.productName = '';
		$scope.nickName = '';
		$scope.productBtnText = 'ADD';		
	};
	
	/**
	 * Pagination 
	 **/
	$scope.pg = {"currentPage":1,"endCount":0,"itemsPerPage":15,"startCount":0,"totalItems":0};	
	$scope.pageChanged=function(){
		$scope.reload();
	};
	
	$scope.productSearch = '';
	$scope.products = [];
	$scope.reload=function(){		
		var urlParams = "?cpage="+$scope.pg.currentPage
				+ "&s="+$scope.strainFilterSelected.id
				+ "&c="+$scope.categoryFilterSelected.id
				+ "&pn="+$scope.productSearch;
		
		$http.get('/inventory/apps/listprods'+urlParams).success(function(data){
			if(data.success){
				$scope.products = data.result;
				if(data.pg){
					$scope.pg = data.pg;
				}
			}
		});	
	};	
	
	$scope.reload();
};