var cartItemCtrlr = function($scope, $http, $rootScope, $timeout){

	var m = $scope.m;
	
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

		if(per && parseInt(per) == per){
			this.item.qty = this.item.qty + parseInt(per);
		}
		
		if(this.item && this.item.qty && this.item.qty > 0){
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
		else if(!this.item.qty || this.item.qty <= 0){
			this.item.qty = 1;
			return false;
		}
		else {
			$scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
		}
	};
	
	
	$scope.qtyPlus = function(){
		this.item.qty++;
		$scope.updateCart.call(this);
	};
	
	
	$scope.qtyMinus = function(){
		this.item.qty--;
		if(this.item.qty < 1) this.item.qty = 1;

		$scope.updateCart.call(this);
	};
	
	
	$scope.addDoubleDown = function(){
		
		var pid = this.prd._id, 
		vid = this.prd.vid;
		
		if(m.appliedCouponCode){
			removeCoupon(m.appliedCouponCode, function(){
				applyDoubleDown(pid, vid);
			});
		}
		else{
			applyDoubleDown(pid, vid);
		}		
	};
	
	
	$scope.addbriteBox = function(){
		
//		if(m.appliedCouponCode){
//			removeCoupon(m.appliedCouponCode, function(){
//				applyBriteBox();
//			});
//		}
//		else{
//			applyBriteBox();
//		}		
	};	
	
	$scope.addFifthFlower = function(){
		
		if(m.appliedCouponCode){
			removeCoupon(m.appliedCouponCode, function(){
				applyFifthFlower();
			});
		}
		else{
			applyFifthFlower();
		}	
	};
	
	$scope.addPromo420 = function(){
		
		if(m.appliedCouponCode){
			removeCoupon(m.appliedCouponCode, function(){
				applyPromo420();
			});
		}
		else{
			applyPromo420();
		}	
	};
	
	var applyDoubleDown = function(pid, vid){
		
		$http.post(_lbUrls.cart + '/adddoubledown/' + pid + '/' + vid)
		.then(function(resp){
			if(resp.data && resp.data.success){
				
				$rootScope.rootCartCount = resp.data.cartCount;
				
				m.order = resp.data.order;
				m.processOrder();
				
				_lbFns.pSuccess('Offer Item Added.');
			}
			else{
				$scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
			}
		},
		function(resp){
			if(resp.status == 403){
				$scope.$parent.$parent.pageLevelAlert  = "Your browser was idle for long. " +
					"Please refresh the page and add the item to cart again.";
			}
			else {
				$scope.$parent.$parent.pageLevelAlert  = "There was some error adding the item. " +
					"Please try later. If problem persists, please call the customer care.";
			}
		});
	},
	
	applyBriteBox = function(){
		
//		$http.post(_lbUrls.cart + '/addbritebox')
//		.then(function(resp){
//			if(resp.data && resp.data.success){
//				
//				$rootScope.rootCartCount = resp.data.cartCount;
//				
//				m.order = resp.data.order;
//				m.processOrder();
//				
//				_lbFns.pSuccess('Brite Box Item Added.');
//			}
//			else{
//				$scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
//			}
//		},
//		function(resp){
//			if(resp.status == 403){
//				$scope.$parent.$parent.pageLevelAlert  = "Your browser was idle for long. " +
//					"Please refresh the page and add the item to cart again.";
//			}
//			else {
//				$scope.$parent.$parent.pageLevelAlert  = "There was some error adding the item. " +
//					"Please try later. If problem persists, please call the customer care.";
//			}
//		});
	},
	
	applyFifthFlower = function(){
		
		$http.post(_lbUrls.cart + '/add-assorted-variety')
		.then(function(resp){
			if(resp.data && resp.data.success){
				
				$rootScope.rootCartCount = resp.data.cartCount;
				
				m.order = resp.data.order;
				m.processOrder();
				
				_lbFns.pSuccess('Holiday offer item added.');
			}
			else{
				$scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
			}
		},
		function(resp){
			if(resp.status == 403){
				$scope.$parent.$parent.pageLevelAlert  = "Your browser was idle for long. " +
					"Please refresh the page and add the item to cart again.";
			}
			else {
				$scope.$parent.$parent.pageLevelAlert  = "There was some error adding the item. " +
					"Please try later. If problem persists, please call the customer care.";
			}
		});
	},
	
	applyPromo420 = function(){
		
		$http.post(_lbUrls.cart + '/add-promo-420')
		.then(function(resp){
			if(resp.data && resp.data.success){
				
				$rootScope.rootCartCount = resp.data.cartCount;
				
				m.order = resp.data.order;
				m.processOrder();
				
				_lbFns.pSuccess('420 Promo applied.');
			}
			else{
				$scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
			}
		},
		function(resp){
			if(resp.status == 403){
				$scope.$parent.$parent.pageLevelAlert  = "Your browser was idle for long. " +
					"Please refresh the page and add the item to cart again.";
			}
			else {
				$scope.$parent.$parent.pageLevelAlert  = "There was some error adding the item. " +
					"Please try later. If problem persists, please call the customer care.";
			}
		});
		
	},
	
	removeCoupon = function(couponCode, cb){

		$scope.couponErrors = '';

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
							
							if(cb) cb();
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
	};
	
	$scope.prdCartClose = function(){
		$('body').removeClass('sidecartOpen').addClass('sidecartClose');
	};	
	
	$scope.couponErrors = '';	
	$scope.removeCoupon = function(){
		removeCoupon(this.item.name, null);
	};


	$scope.applyCoupon = function(){

		$scope.couponErrors = '';
		//console.log("m.couponCode - " +m.couponCode);
		if(m.couponCode){
			$http.get(_lbUrls.cart + 'applycoupon/' + m.couponCode,{
				params : {
					'hidepop' : true,
					'oid' : m.order._id
				}
			})
			.then(
					function(resp){
						if(resp.data && resp.data.success){
							_lbFns.pSuccess('Promocode applied');
							m.couponCode = '';

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
	
	$scope.clearCouponErrorMsg = function(){
		$scope.couponErrors = '';
	};
	
	$scope.useThisCoupon = function(){
		if(this.promo){
			m.couponCode = this.promo;
			$scope.applyCoupon();
		}
	};
	
	
	$scope.allPromos = [];
	$scope.promos = [];
	
	var fixAvailableCoupons = function(){	
		$scope.promos = $scope.allPromos.slice();
	
		if(m.order.lineItems) {
			
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
		}
		
		if($scope.promos.length===0){
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
	
	$(document).on('click','.cart-promo-options .fa-label',function(){

		if($('.cart-promo-options.promoSelected').length === 0) return;
		
		var x = $('.cart-promo-options.promoSelected').offset().top,
		offset = parseInt($('.header-offset').css('margin-top'));
		$("html, body").animate({'scrollTop': (x - offset)},400);
	});
};