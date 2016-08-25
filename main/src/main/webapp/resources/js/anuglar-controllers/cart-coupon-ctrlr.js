var cartCouponCtrlr = function($scope, $http, $rootScope){
	
	var m = $scope.m;
	
	$scope.couponErrors = '';
	
	$scope.removeCoupon = function(){

		$scope.couponErrors = '';
		var couponCode = this.item.name;

		if(couponCode){
			$http.get(_lbUrls.cart + 'removecoupon/' + couponCode,{
				params : {
					'hidepop' : true,
					'oid' : m.order._id
				}
			})
			.then(
					function(resp){
						if(resp.data && resp.data.success){
							_lbFns.pSuccess('Promocode removed');

							m.order = resp.data.order;
							m.processOrder();
							fixAvailableCoupons();
						}

						else if(resp.data && resp.data.message){
							$scope.couponErrors = resp.data.message;
						}
						else{
							$scope.couponErrors = $rootScope.errMsgPageRefresh;
						}
					},

					function(){
						$scope.couponErrors = $rootScope.errMsgPageRefresh;
					}
			);
		}
		else{
			$scope.couponErrors = "Invalid promo code";
		}

	}


	$scope.applyCoupon = function(){

		$scope.couponErrors = '';

		if($scope.couponCode){
			$http.get(_lbUrls.cart + 'applycoupon/' + $scope.couponCode,{
				params : {
					'hidepop' : true,
					'oid' : m.order._id
				}
			})
			.then(
					function(resp){
						if(resp.data && resp.data.success){
							_lbFns.pSuccess('Promocode applied');
							$scope.couponCode = '';

							m.order = resp.data.order;
							m.processOrder();
							fixAvailableCoupons();
						}

						else if(resp.data && resp.data.message){
							$scope.couponErrors = resp.data.message;
						}
						else{
							$scope.couponErrors = 'Invalid promo code';
						}
					},

					function(){
						$scope.couponErrors = $rootScope.errMsgPageRefresh;
					}
			);
		}
		else{
			$scope.couponErrors = "Invalid promo code";
		}

	};
	
	
	$scope.useThisCoupon = function(){
		if(this.promo){
			$scope.couponCode = this.promo;
			$scope.applyCoupon();
		}
	};
	
	
	$scope.allPromos = [];
	$scope.promos = [];
	
	var fixAvailableCoupons = function(){	
		$scope.promos = $scope.allPromos.slice();
	
		for(var i=0; i<m.order.lineItems.length; i++){
			var item = m.order.lineItems[i];
			if(item.type=='coupon'){
				
				for(var j=0; j<$scope.promos.length; j++){
					if($scope.promos[j] == item.name){
						$scope.promos.splice(j,1);
						break;
					}
				}
			}
		}
		
		if($scope.promos.length==0){
			m.promosAvailable = false;
		}
		else{
			m.promosAvailable = true;
		}
	},
	
	getCustomerCoupons = function(){
		m.promosAvailable = false;

		$http.get(_lbUrls.cart + 'getcpromos', {
			params : {
				'hidepop' : true
			}
		})
		.then(
			function(resp){
				if(resp.data && resp.data.success){
					m.promosAvailable = true;
					$scope.allPromos = resp.data.results;
					fixAvailableCoupons();
				}
				else{
					$scope.promos = [];
				}
			},

			function(){
				$scope.promos = [];
			}
		);
	};
	
	if(m.user && m.user._id){
		getCustomerCoupons();
	}
};