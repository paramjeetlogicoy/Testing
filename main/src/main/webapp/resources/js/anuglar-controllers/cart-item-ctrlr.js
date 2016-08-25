var cartItemCtrlr = function($scope, $http, $rootScope, $timeout){

	$scope.itsOffHour = false;
	
	var m = $scope.m,
	
	checkOffHours = function(){
		
		if(m.emptyCart){
			console.log('checkoffhours cancelled');
			return;
		}
		
		var currHour = (new Date()).getHours();		
		if(currHour >= 23 || currHour < 11){ 
			$scope.itsOffHour = true;
		}  
		else{
			$scope.itsOffHour = false;
		}
		
		$timeout(checkOffHours, 1000 * 60); //check everyminute
	};	
	checkOffHours(); //run for the first time;
	
	$scope.itemRemove = function(){
		
		$scope.$parent.$parent.pageLevelAlert='';
		
		if(m.order && this.item){
			var req = {
					method: 'DELETE',
					url: _lbUrls.cart + 'removeitem',
					params: {'oid' : m.order._id, 
						'pid' : this.item.productId,
						'vid' : this.item.variationId
					}
			};
			
			$http(req)
			.then(
					function(resp){
						if(resp.data && resp.data.success){
					
							$rootScope.rootCartCount = resp.data.cartCount;
							
							m.order = resp.data.order;
							m.processOrder();
							
							if(m.cartPage) _lbFns.pSuccess('Item removed.');
						}
					
						else if(resp.data && resp.data.message){
							if(m.cartPage) $scope.$parent.$parent.pageLevelAlert = resp.data.message;
						}
						else{
							if(m.cartPage) $scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
						}
					},
					
					function(){
						if(m.cartPage) $scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
					}
			);
		}
		else {
			if(m.cartPage) $scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
		}
	};
	
	
	$scope.updateCart = function(per){
		
		$scope.$parent.$parent.pageLevelAlert = '';
		
		if(!m.cartPage && per){
			this.item.qty = this.item.qty + parseInt(per);
		}
		
		if(this.item && this.item.qty && this.item.qty !=0){
			$http.get(_lbUrls.cart + 'updatecart', {
				params : { 
					'oid' : m.order._id,
					'vid' : this.item.variationId,
					'pid' : this.item.productId,
					'qty' : this.item.qty
				}
			})
			.then(function(resp){
				if(resp.data && resp.data.success){
					
					$rootScope.rootCartCount = resp.data.cartCount;
					m.order = resp.data.order;
					m.processOrder();
					
					if(m.cartPage) _lbFns.pSuccess('Order Updated.');		
				}
				else{
					if(m.cartPage) $scope.$parent.$parent.pageLevelAlert = resp.data.message;
				}
			});
		}
		else if(!this.item.qty){
			$scope.$parent.$parent.pageLevelAlert = "Please provide a valid quantity";
			return false;
		}
		else {
			$scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
		}
	};
	
	
	$scope.addDoubleDown = function(){
		
		$http.post(_lbUrls.cart + '/adddoubledown/' + this.prd._id)
		.then(function(resp){
			if(resp.data && resp.data.success){
				
				$rootScope.rootCartCount = resp.data.cartCount;
				
				m.order = resp.data.order;
				m.processOrder();
				
				_lbFns.pSuccess('Offer Item Added.');
			}
		
			else if(resp.data && resp.data.message){
				if(resp.data.message.indexOf('/product')>-1){
					location.href = resp.data.message;
				}
				else{
					$scope.$parent.$parent.pageLevelAlert = resp.data.message;
				}
			}
			else{
				$scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
			}
		},
		function(resp){
			if(resp.status == 403){
				$scope.$parent.$parent.pageLevelAlert  = "Your browser was idle for long. "
					+"Please refresh the page and add the item to cart again.";
			}
			else {
				$scope.$parent.$parent.pageLevelAlert  = "There was some error adding the item. "
					+"Please try later. If problem persists, please call the customer care.";
			}
		});
	};	
	
	$scope.prdCartClose = function(){
		$('body').removeClass('sidecartOpen').addClass('sidecartClose');
	};
};