var allProductPriceCtrlr = function($scope, $http, $rootScope, $timeout){

	$scope.itemTotal = 0;
        $scope.productDetail = [];
        
	$scope.calculateItemTotal = function(){
               
               for(var j=0; j<$scope.prices.length; j++){
                    var currHold = $scope.prices[j];
                    if($scope.productDetail.length > 0){
                        var productDetail = $scope.productDetail[j];
                        
                        if(productDetail.total_remain_qty){
                              var qty =  parseInt(currHold.qty);
                              var total_remain_qty =  parseInt(productDetail.total_remain_qty);
                              if((qty > total_remain_qty)){
                                  $scope.prices[j].qty = total_remain_qty;                                  
                              }else if(qty === undefined || isNaN(qty)) {
                                  $scope.prices[j].qty = 0;                                
                              }
                        }
                    }                   
		}          
               
                if($scope.prices.length)
		$scope.itemTotal = 0;
		//Set the quantity as zero;
		for(var i=0; i<$scope.prices.length; i++){
			var curr = $scope.prices[i];
			if(curr.qty !== 0){
				if(curr.salePrice){
					$scope.itemTotal+=(curr.salePrice*curr.qty);
				}
				else{
					$scope.itemTotal+=(curr.regPrice*curr.qty);
				}
			}
		}
	};
	
         $scope.getProDetailFromInvByID = function (index,productId){
          
            var data = ''+productId;
           
            var config = {
                headers : {                   
                    'Authorization': 'Basic eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiTWFpbiBBZG1pbiIsImlzcyI6ImF1dGgwIiwidXNlcnR5cGUiOiJvcGVyYXRvciIsInVzZXJuYW1lIjoiY2hhbXAifQ.muZ0ZV2SFNFnjgXhHhuk0iCM7klZ5PJIJq5U07QM8LQ'
                }
            };
            
            $http.post('/inventory/apps/acceptproductlist', productId, config)
            .success(function (data, status, headers, config) {
                $scope.prices.productDetail = {};
                if(data.success === true){
                   // $scope.prices.productDetail= data.result[0];
                   if(data.result.length > 0){
                        $scope.productDetail[index] = data.result[0];
                        console.log('===============');
                        console.log($scope.productDetail);
                   }                   
                }
                console.log($scope.prices);
            }).error(function (data, status, header, config) {               
            });            
        };
        
        
	$scope.itemError = '';
	$scope.itemSuccess = '';
	$scope.quickAddToCart = function(event){
		
		$scope.itemError = '';
		$scope.itemSuccess = '';
		
		var $angularElement = angular.element(event.target).closest('li'),
		
		productName = $angularElement.data('pname'),
		productId = $angularElement.data('pid'),
		featuredImg = $angularElement.data('img'),
		
		lineItems = [];		
		
		for(var i=0; i<$scope.prices.length; i++){
			var curr = $scope.prices[i];
			if(curr.qty !== 0){

				lineItems.push({
					name : productName,
					qty : curr.qty,
					productId : productId,
					img : curr.img ? curr.img : featuredImg,
					variationId : curr._id ? curr._id : 0
				});
			}
		}
			
		if(lineItems.length===0){
			$scope.itemError = 'Please select quantity for atleast one item.';
			return false;
		}

		$scope.addingToCart = true;
		
		//Reset Messages
		$scope.itemError = '';
		$scope.itemSuccess = '';
		$scope.$parent.setPageAlert('');
		
		$http.post(_lbUrls.addtocart+'/multi/?hidepop', lineItems)
		.then(function(resp){
			
			if(resp.data && resp.data.success){
				
				$scope.productInCart = resp.data.productCount;
				$rootScope.rootCartCount = resp.data.cartCount;
				$.cookie('lbbagnumber', resp.data.orderId, {expires:30, path:'/'});
				$scope.itemSuccess = 'Item(s) added to cart.';
				
				$timeout(function () {
					$scope.itemSuccess = "";
			    }, 4000);
				
				//Reset Quantity
				for(var i=0; i<$scope.prices.length; i++){
					$scope.prices[i].qty = 0;
				}
				
				//Reset itemTotal
				$scope.itemTotal = 0;
				
				//Send signal for sidecart load;
				$rootScope.$broadcast('mCartReload');
			}
			else {
				$scope.itemError  = resp.data.message;
			}

			$scope.addingToCart = false;
		},
		function(resp){
			if(resp.status == 403){
				$scope.$parent.setPageAlert("Your browser was idle for long. " +
					"Please refresh the page and add the item to cart again.");
			}
			else {
				$scope.$parent.setPageAlert("There was some error creating the order. " +
					"Please try later. If problem persists, please call the customer care.");
			}

			$scope.addingToCart = false;
		});		
		
		
	};
	
	var scopeInit = function(){		
               
		$scope.prices = $scope.$parent.prices;
		               
		//Sort the data
		$scope.prices.sort(function(a,b){return a.regPrice - b.regPrice;});
		
		var itemsOutofStock = true;
		//Check stock status and Set the quantity as zero;
		for(var i=0; i<$scope.prices.length; i++){
			$scope.prices[i].qty = 0;
			
			if($scope.prices[i].stockStat=='instock')
				itemsOutofStock =false;
		}
		
		$scope.itemsOutofStock = itemsOutofStock;

		//save price for future uses.
		$scope.$parent.productPrices[$scope.$parent.currentPid] = $scope.prices;
		
		//calculate new total
		$scope.calculateItemTotal();
                
                for(var j=0; j<$scope.prices.length; j++) {
                    var currHold = $scope.prices[j];                    
                    $scope.getProDetailFromInvByID(j,currHold.productId);
		}
	};
	
	scopeInit();
};