var
addPurchaseCtrlr = function($scope, $http, $filter, $rootScope, $uibModal, currentUser){
	
	$rootScope.rootPage = "Purchases";
	$scope.controlId = currentUser.ctrlid;
	$scope.purchaseBtnText = 'ADD';
	$scope.currentPurchaseId = 0;
	
	var dn = new Date();
	$scope.dateAdded = new Date(dn.getFullYear(),dn.getMonth(),dn.getDate());

	$scope.sku = '';
	$scope.products = [];
	$http.get('/inventory/apps/listallprods').success(function(data){
		if(data.success){
			$scope.products = data.result;
			$scope.prodSelected = $scope.products[0];
		}
	});
	
	
	$scope.vendors = [];
	$scope.filterVendors = [];
	$scope.vendorFilterSelected = {'id':0,'vendorName':'All'};
	$http.get('/inventory/apps/listvendors').success(function(data){
		if(data.success){
			$scope.vendors = data.result;
			$scope.vendorSelected = $scope.vendors[0];
			
			$scope.filterVendors = $scope.vendors.slice();
			$scope.filterVendors.unshift($scope.vendorFilterSelected);
		}
	});
	
		
	$scope.addPurchase=function(){
		
		if($scope.currentPurchaseId!=0){			
			for(var i=0;i<$scope.purchases.length;i++){
				if($scope.purchases[i].id==$scope.currentPurchaseId){					
					$scope.purchases[i].quantity = $scope.quantity;
					$scope.purchases[i].weightInGrams = $scope.weightInGrams;
					$scope.purchases[i].unitPrice = $scope.unitPrice;
					$scope.purchases[i].growthCondition = $scope.growthCondition;
					$scope.purchases[i].operatorComments = $scope.operatorComments;
					
					$scope.purchases[i].productId = $scope.prodSelected.id;
					$scope.purchases[i].productName = $scope.prodSelected.productName;
					
					$scope.purchases[i].vendorId = $scope.vendorSelected.id;
					$scope.purchases[i].vendorName = $scope.vendorSelected.vendorName;
					//$scope.purchases[i].purchaseCode = $scope.purchaseCode;
					
					$scope.purchases[i].dateAdded = $filter('date')($scope.dateAdded, 'yyyy-MM-dd');

					break;
				}
			}
			
			$.post('/inventory/apps/updatepurchase', {id:$scope.currentPurchaseId,
						qty:$scope.quantity, wt:$scope.weightInGrams,
						price:$scope.unitPrice, gc:$scope.growthCondition,
						oc:$scope.operatorComments, date:$filter('date')($scope.dateAdded, 'yyyy-MM-dd'),
						//pc:$scope.purchaseCode,
						pi:$scope.prodSelected.id, vi:$scope.vendorSelected.id,
						opsid:_luvbriteGlobalOpsId},
				function(data){
					if(!data.success) alert(data.message);
				}
			);			
			
			$scope.currentPurchaseId = 0;
			$scope.quantity = '';
			$scope.weightInGrams = 0;
			$scope.unitPrice = '';
			$scope.growthCondition = '';
			$scope.operatorComments = '';
			//$scope.purchaseCode = '';
			
			dn = new Date();
			$scope.dateAdded = new Date(dn.getFullYear(),dn.getMonth(),dn.getDate());
				
			$scope.purchaseBtnText = 'ADD';
		}
		else {
			
			$scope.purchases.push({
				id:'999999', 
				productId : $scope.prodSelected.id,
				productName : $scope.prodSelected.productName,
				
				vendorId : $scope.vendorSelected.id,
				vendorName : $scope.vendorSelected.vendorName,
				
				quantity : $scope.quantity, 
				weightInGrams : $scope.weightInGrams,
				unitPrice : $scope.unitPrice, 
				growthCondition : $scope.growthCondition,
				operatorComments : $scope.operatorComments,
				//purchaseCode : $scope.purchaseCode,
				dateAdded : $filter('date')($scope.dateAdded, 'yyyy-MM-dd')
			});
			
			$.post('/inventory/apps/newpurchase',{qty:$scope.quantity, wt:$scope.weightInGrams,
						price:$scope.unitPrice, gc:$scope.growthCondition,
						oc:$scope.operatorComments, date:$filter('date')($scope.dateAdded, 'yyyy-MM-dd'),
						pi:$scope.prodSelected.id, vi:$scope.vendorSelected.id,
						//pc:$scope.purchaseCode,
						pn:$scope.prodSelected.productName, vn : $scope.vendorSelected.vendorName,
						opsid:_luvbriteGlobalOpsId},
						
				function(data){
					if(data.success){
						for(var i=0;i<$scope.purchases.length;i++){
							if($scope.purchases[i].id=='999999'){
								$scope.purchases[i].dateAdded = data.purchase.dateAdded;
								$scope.purchases[i].id = data.purchase.id;
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

			$scope.currentPurchaseId = 0;
			$scope.quantity = '';
			$scope.weightInGrams = 0;
			$scope.unitPrice = '';
			$scope.growthCondition = '';
			$scope.operatorComments = '';
			//$scope.purchaseCode = '';
			
			dn = new Date();
			$scope.dateAdded = new Date(dn.getFullYear(),dn.getMonth(),dn.getDate());
		}
		
	};
	
	$scope.editPurchase=function($event){
		$event.preventDefault();		
		
		$scope.currentPurchaseId = this.purchase.id;
		$scope.quantity = this.purchase.quantity;
		$scope.weightInGrams = this.purchase.weightInGrams;
		$scope.unitPrice = this.purchase.unitPrice;
		$scope.growthCondition = this.purchase.growthCondition;
		$scope.operatorComments = this.purchase.operatorComments;
		//$scope.purchaseCode = this.purchase.purchaseCode;
		
		dn = new Date(Date.parse(this.purchase.dateAdded));
		$scope.dateAdded = new Date(dn.getFullYear(),dn.getMonth(),dn.getDate());
		
		for(var i=0;i<$scope.products.length;i++){
			if($scope.products[i].id==this.purchase.productId){
				$scope.prodSelected = $scope.products[i];
				break;
			}
		}
		
		for(var i=0;i<$scope.vendors.length;i++){
			if($scope.vendors[i].id==this.purchase.vendorId){
				$scope.vendorSelected = $scope.vendors[i];
				break;
			}
		}
		
		$scope.purchaseBtnText = 'SAVE';
		
		/**
		 * Scroll up to the form and show subtle color change.
		 **/
		_lbFns.showViewForm();
	};
	
	$scope.cancelEdit=function($event){
		$event.preventDefault();
		
		$scope.currentPurchaseId = 0;
		$scope.quantity = '';
		$scope.weightInGrams = 0;
		$scope.unitPrice = '';
		$scope.growthCondition = '';
		$scope.operatorComments = '';
		//$scope.purchaseCode = '';

		dn = new Date();
		$scope.dateAdded = new Date(dn.getFullYear(),dn.getMonth(),dn.getDate());
		
		$scope.purchaseBtnText = 'ADD';		
	};	
	
	
	/**Add Adjustment*/
	$scope.addAdjustment=function(){
		
	    var modalInstance = $uibModal.open({
	      templateUrl: '/resources/ng-templates/admin/inv/modals/inv-adjustment.html'+versionCtrl,
	      controller: 'ModalInvAdjCtrl',
	      backdrop : 'static',
	      resolve: {
	    	modalScope: function () {
	        	return {products:$scope.products, controlId:$scope.controlId};
	        }
	      }
	    });
	
	    modalInstance.result.then(
	    	function (){
	    		$scope.reload();
	    	},
	    	function(){}
	    );		
	};

	
	/**Pagination Logic**/
	$scope.pg = {"currentPage":1,"endCount":0,"itemsPerPage":0,"startCount":0,"totalItems":0};	
	
	$scope.productSearch = '';
	$scope.sortBy = '';
	$scope.sdir = 'ASC'	
	$scope.pageChanged=function(){
		$scope.reload();
	};
	/***/
	
	$scope.sortCol=function(param, e){
		//generic fn defined in main.js
		sortColFns($scope, param, e);
	};
	
	$scope.purchases = [];
	$scope.reload=function(){
		var urlParams = "?cpage="+$scope.pg.currentPage
		+ "&sort="+$scope.sortBy
		+ "&sdir="+$scope.sdir
		+ "&v="+$scope.vendorFilterSelected.id
		+ "&pc="+$scope.sku
		+ "&pn="+$scope.productSearch;

		$http.get('/inventory/apps/listpurchases'+urlParams).success(function(data){
			if(data.success){
				$scope.purchases = data.result;
				if(data.pg){
					$scope.pg = data.pg;
				}
			}
		});		
	};	

	$scope.reload();

};;