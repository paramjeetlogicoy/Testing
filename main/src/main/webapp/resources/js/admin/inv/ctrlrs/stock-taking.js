var stockTakeCtrlr = function($scope, $http, $rootScope, $timeout, currentUser){

	$rootScope.rootPage = "Stock";
	$scope.controlId = currentUser.ctrlid;
	
	if($scope.controlId>100) return;	
	
	$scope.productCode = "";
	$scope.productSelected = {};
	$scope.searchOn = false;
	$scope.stockInfoFound = true;
	
	$scope.difference = 0;
	$scope.adjRsn = '';
	$scope.successMsg = '';
	
	$scope.pos = [];
	$http.get('/inventory/apps/listallprods').success(function(data){
		if(data.success && data.result.length > 0){
			$scope.pos = data.result;

			$timeout(function(){$('#productCode').focus();});
		}
	});
	
	$scope.getProducts = function(){		
		if($scope.productCode!="")
			$scope.searchOn = true;
	};
	
	$scope.pinfo = {};
	$scope.getStock = function(){
		$scope.searchOn = false;
		
		if($scope.productCode != '' 
			&& $scope.productCode == parseInt($scope.productCode)){
			$scope.productSelected.id = $scope.productCode;
		}
		
		if(!$scope.productSelected || !$scope.productSelected.id){
			$scope.stockInfoFound = false;
			$scope.productSelected = {};	
			return;
		}
		
		$http.get('/inventory/apps/listshopinv',{
			params: {
				pid:$scope.productSelected.id, 
			}
		})
		.success(function(res){			
			if(res.success){	
				if(res.result.length>0){					
					$scope.pinfo = res.result[0];
					$scope.pinfo.product = $scope.productSelected;
					
					$scope.stockInfoFound = true;
					$scope.productSelected = {};
					$scope.productCode = "";
					
					$timeout(function(){$('.actuals').focus();})
				}
				else{
					$scope.stockInfoFound = false;
				}
			}
			else{
				alert(res.message);
			}
		}).error(function(){
			alert("There was some error getting the stock info.");
		});
		
	};
	
	$scope.selectThisProduct = function(){
		$scope.searchOn = false;
		$scope.productSelected = $.extend(true,{},this.po);
		$scope.productCode = $scope.productSelected.productName;
		$scope.pinfo = {};
		$scope.getStock();
	};
	
	
	$scope.calculateDiff = function(){
		$scope.difference = parseFloat($scope.pinfo.remainingTotal) 
							- parseFloat($scope.pinfo.actual);
		
		if(isNaN($scope.difference))
			$scope.difference = 0;	
		
		if($scope.difference != 0)
			$scope.getPrice();
	};
	
	$scope.price = '';
	$scope.weight = '';
	$scope.prices = [];
	$scope.weights = [];
	$scope.getPrice = function(){
		
		$http.get('/inventory/apps/listprodpriceweight',{
			params:{
				pid:$scope.pinfo.product.id
			}
		})
		.success(function(data){
			if(data.success){
				
				//Reset the data
				$scope.price = '';
				$scope.weight = ''; 
				
				$scope.prices = data.prices;				
				if($scope.prices.length==1){
					$scope.price = $scope.prices[0];
				}
				
				//Populate the weights only if its not flower.
				if($scope.pinfo.product.categoryId!=1){
					$scope.weights = data.weights;
					
					if($scope.weights.length==1){
						$scope.weight = $scope.weights[0];
					}
				}
			}
		});
	};
	
	
	$scope.saveAdj = function(){

		var adjType = "add";
		if($scope.difference>0){
			adjType = "red";
		}
		
		var qty = 1, wt = 1, cid = 0;
		if($scope.pinfo.flower){
			wt = $scope.difference;
			cid = 1;
		}
		else{
			wt = $scope.weight;
			qty = $scope.difference;
		}
		
		var req = {
				method: 'POST',
				url: '/inventory/apps/newinvadj',
				params: {
					pid:$scope.pinfo.product.id,
					rsn:$scope.adjRsn,
					type:adjType, 
					wt:wt, 
					qty:qty,
					price:$scope.price,
					cid:cid,
					opsid:_luvbriteGlobalOpsId
				}
		};
		
		$http(req).then(
				function(response){
					var data = response.data;

					if(data.success){
						$scope.successMsg = "Adjusted Added. Stock Updated."
						
						$scope.difference = 0
						$scope.productSelected = $scope.pinfo.product;
						
						$scope.pinfo = {};
						$scope.getStock();
					}
					else{
						alert(data.message);
					}
				},
				function(){
					alert('There was some error processing your request.');
				}
		);
	};
	
};