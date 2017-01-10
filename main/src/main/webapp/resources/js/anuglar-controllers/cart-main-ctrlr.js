var cartMainCtrlr = function($scope, $http, $templateRequest, $compile, $rootScope){
	var m = this,
	deliveryLoaded = false,	
	cmcVersion = Math.random(); //'v0002'; //Math.random();
	
	m.order = {};	
	m.ddprds = [];
	m.user = {};
	m.orderMin = 9999;
	m.config = {};
	m.sales = [];
	
	m.cartPage = false;
	m.cartLoading = true;
	m.pmtCOD = false;
	
	m.cartData = null;
	m.ccErrors = '';
	m.promoOptions = '';
	m.couponCode = '';

	$scope.pageLevelAlert='';
	
	if(_globalUserId) m.user._id = _globalUserId;
	
	
	m.promosAvailable = false;
	m.couponApplied = true;  //Only refers to coupons
	m.appliedCouponCode = "";
	m.offersApplied = false; //Refers to all kinds of offers
	m.orderAboveOrderMin = false;
	m.emptyCart = true;
	m.addressSaved = false;
	m.codEnabled = true;
	m.ccPmtEnabled = false;
	
	m.doubleDownActive = false;
	m.doubleDownApplied = false;
	m.doubleDownEligible = false;
	
	m.flowerCount = 0;
	m.flowerIds = [];
	m.fifthFlowerActive = false;
	m.fifthFlowerApplied = false;
	m.fifthFlowerEligible = false;
	
	m.briteBoxApplied = false;
	m.briteBoxEligible = false;
	m.briteBoxThreshold = 75;
	m.availableDeals = {};
	
	m.showPromoTab = true;
	
	
	/* When ever there is a change in m.order, this function needs to be called 
	 * most of the logic control flags are set here. */
	m.processOrder = function(){
		
		m.sales = [];	
		m.appliedCouponCode = "";	
		m.promoOptions = '';
		
		var couponApplied = false,
			offersApplied = false,
			doubleDownApplied = false,
			briteBoxApplied = false,
			fifthFlowerApplied = false,
			emptyCart = true,
			flowerCount = 0;
		
		
		if(m.order.lineItems && m.order.lineItems.length>0){
			m.order.lineItems.forEach(function(item){
				
				if(item.instock){
					
					if(item.type=='item'){					
						emptyCart = false;
						
						if(item.img){
							item.img = item.img.replace('.jpg','-150x150.jpg');
						}
						
						//Count the flowers in the orders
						if(item.productId != 10504 		&&   //Double down offer flower 1 Gram 
							item.productId != 11871 	&& //Holiday special offer flower
							m.flowerIds.indexOf(item.productId) > -1){
							
							flowerCount = flowerCount + item.qty;
						}
					}
					
					
					if(item.type=='coupon'){					
						couponApplied = true;
						m.appliedCouponCode = item.name;
					}
					
					
					if(item.promo && item.promo !== ''){					
						offersApplied = true;
					}
					
					if(item.type=='item' && 
							(item.promo == 's' || 
									item.promo == 'doubledownoffer'  || 
									item.promo == 'firsttimepatient' || 
									item.promo == 'fifthflower')){
						
						//var productName = item.name.length>15?item.name.substr(0,15)+'...':item.name,
						var productName = item.name,
						discount = (item.cost - item.price)*item.qty;
						
						
						if(item.promo == 'doubledownoffer'){
							productName = 'Double Down Offer';
							doubleDownApplied = true;
						}
						
						
						else if(item.promo == 'firsttimepatient'){
							productName = 'First Time Patient';
							briteBoxApplied = true;
						}
						
						
						else if(item.promo == 'fifthflower'){
							productName = 'Holiday Special Offer';
							fifthFlowerApplied = true;
						}
						
							
						
						if(!isNaN(discount) && discount > 0){						
							m.sales.push({
								'name' : productName,
								'price' : discount
							});
						}
						
					}
				}
			});
		}	
		
		m.doubleDownApplied = doubleDownApplied;
		m.couponApplied = couponApplied;
		m.offersApplied = offersApplied;
		m.emptyCart = emptyCart;
		m.briteBoxApplied = briteBoxApplied;
		m.fifthFlowerApplied = fifthFlowerApplied;
		m.flowerCount = flowerCount;
		
		//OrderMin Check (it doesnt apply to orders placed by orders@luvbrite.com [_id = 29])
		if(m.order.total && (m.order.total >= m.orderMin || 
				(m.user && (m.user._id == 29 || m.user._id == 1)))){
			
			m.orderAboveOrderMin = true;
			
			if(m.cartPage && !deliveryLoaded) m.loadDeliveryTemplate();			
		} 
		else {			
			m.orderAboveOrderMin = false;
			
			if(m.cartPage) m.destroyDeliveryTemplate();
		}
		
		
		//DoubleDown Check
		if(m.doubleDownActive && !m.doubleDownApplied){
			m.doubleDownEligible = true;
		}
		else {
			m.doubleDownEligible = false;
		}	
		
		
		//BriteBox check
		if(m.availableDeals.firstTimepatient && !m.briteBoxApplied){
			m.briteBoxEligible = true;
		}
		else {
			m.briteBoxEligible = false;
		}
		
		
/*		console.log("1" + (m.briteBoxEligible && (m.order.total >= m.briteBoxThreshold)));
		console.log("2" + (m.doubleDownEligible && (m.order.total >= m.config.doubleDown)));
		console.log("3" + m.promosAvailable);
		console.log("4" + !m.couponApplied);*/
		
		//Promotab
		if((m.briteBoxEligible && (m.order.total >= m.briteBoxThreshold)) || 
			(m.doubleDownEligible && (m.order.total >= m.config.doubleDown)) || 
			(m.fifthFlowerActive && !m.fifthFlowerApplied && (m.flowerCount >= 4)) || 
			(m.promosAvailable && !m.couponApplied) || 
			!m.couponApplied){
			
			m.showPromoTab = true;
		}
		else{
			m.showPromoTab = false;
		}
		
		
		//Set Britebox as the default if its firsttime patient.
		if(!m.briteBoxApplied && m.availableDeals.firstTimepatient){
			m.promoOptions = 'firsttimepatient';
		}
		else if(m.fifthFlowerActive && !m.fifthFlowerApplied && (m.flowerCount >= 4)){
			m.promoOptions = 'fifthflower';
		}
		else if(m.doubleDownEligible && (m.order.total >= m.config.doubleDown)){
			m.promoOptions = 'doubledown';
		}
		else if(m.promosAvailable){
			m.promoOptions = 'loyalist';
		}
		else if(!m.couponApplied){
			m.promoOptions = 'coupon';
		}
		
		
		if(m.cartPage && m.emptyCart){			
			//Will destory item, coupon, delivery, payment and review (if exists)!
			m.destroyItemTemplate();
		}
		
		
		//If the fifthflower promo is applied, but is not eligible anymore, remove the item
		if(m.fifthFlowerApplied && m.flowerCount < 4){
			if(m.order && m.order._id){
				var req = {
						method: 'DELETE',
						url: _lbUrls.cart + 'removeitem',
						params: {'oid' : m.order._id, 
							'pid' : 11871,
							'vid' : 0
						}
				};
				
				$http(req)
				.then(
					function(resp){
						if(resp.data && resp.data.success){					
							$rootScope.rootCartCount = resp.data.cartCount;							
							m.order = resp.data.order;
							m.processOrder();
						}
					}
				);
			}
		}
		
	};
	
	
	m.getCart = function(){
		$http.get(_lbUrls.getcart, {
			params : { 
				'hidepop' : true  //tells the config not to show the loading popup
			}
		})
		.then(
				//GetCartSuccess
				function(resp){
			
					m.cartLoading = false;
					m.order = resp.data.order;
					
					/*If we have customer info, set it to proper scope variable*/
					m.user = resp.data.user?resp.data.user:{};
					
					/*General Control configurations*/
					if(resp.data.config){			
						m.config = resp.data.config;
						if(m.config.orderMinimum && m.config.orderMinimum > 0){				
							m.orderMin = m.config.orderMinimum;
						}
						
						/* if we have a value > 0 for double down and we have 
						 * products for double down	*/
						if(m.config.doubleDown && (m.config.doubleDown > 0) && 
								resp.data.ddPrds && (resp.data.ddPrds.length > 0)){
							
							m.doubleDownActive = true;
							m.ddprds = resp.data.ddPrds;
						}
					}	
					
					if(resp.data.availableDeals) m.availableDeals = resp.data.availableDeals;
					
					if(resp.data.commonList){
						if(m.fifthFlowerActive) m.flowerIds = resp.data.commonList;
					}

					if(m.order){ 
						m.processOrder();
					}
					
					
					if(m.cartPage && !m.emptyCart){				
						m.loadItemTemplate();						
						m.loadCouponTemplate();
					}
					else if(!m.emptyCart && !m.cartPage){
						$('body').removeClass('sidecartClose').addClass('sidecartOpen');
					}
					else if(m.emptyCart && !m.cartPage){
						if($('body').hasClass('.sidecartOpen'))
								$('body').removeClass('sidecartOpen').addClass('sidecartClose');
					}
				}, 
				
				//GetCartError
				function(){
					m.cartLoading = false;
				}
		);
	};
	
	//Check if loaded from cart page
	if(angular.element('.cartPage').size()>0){
		m.cartPage = true;
	}	
	m.getCart();
	
	//Trigger cart load for sidecart from allProductPriceCtrlr
	$scope.$on('mCartReload', function(){
		m.getCart();
	});
		
	
	//Load item template
	m.loadItemTemplate = function(){
		
		$scope.ivm = $scope.$new();
				
		$templateRequest("/resources/ng-templates/cart/item.html?" + cmcVersion)
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('#itemCtrlr')
		      .append(template);
		      
		      $compile(template)($scope.ivm);
		 });
	};
	
	//destroy item template
	m.destroyItemTemplate = function(){		
		if($scope.ivm){
			$scope.ivm.$destroy();
		}
		angular.element('#itemCtrlr').empty();
		
		//destroy other templates as well (cascaded action)
		m.destroyDeliveryTemplate();
		m.destroyCouponTemplate();
	};
		
	
	
	
	
	//Load coupon template
	m.loadCouponTemplate = function(){
		
		$scope.cvm = $scope.$new();
			
		$templateRequest("/resources/ng-templates/cart/coupon.html?" + cmcVersion)
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('#couponCtrlr')
		      .append(template);
		      
		      $compile(template)($scope.cvm);
		 });
	};
	
	//destroy coupon template
	m.destroyCouponTemplate = function(){		
		if($scope.cvm){
			$scope.cvm.$destroy();
		}
		angular.element('#couponCtrlr').empty();
	};
		
	
	
	
	
	//Load delivery template
	m.loadDeliveryTemplate = function(){
		
		$scope.dvm = $scope.$new();
				
		$templateRequest("/resources/ng-templates/cart/delivery.html?" + cmcVersion)
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('#deliveryCtrlr')
		      .append(template);
		      
		      $compile(template)($scope.dvm);
		      
		      deliveryLoaded = true;
		 });
	};
	
	//destroy delivery template 
	m.destroyDeliveryTemplate = function(){
		if($scope.dvm){
			$scope.dvm.$destroy();
		}		
		angular.element('#deliveryCtrlr').empty();
	    deliveryLoaded = false;
		
		//Destroy payment 
		m.destroyPaymentTemplate();
		
		//Destroy related 'm' fields
		m.addressSaved = false;
	};
	
	
	
	
	//Load payment template
	m.loadPaymentTemplate = function(){
		
		$scope.pvm = $scope.$new();
			
		$templateRequest("/resources/ng-templates/cart/payment.html?" + cmcVersion)
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('#paymentCtrlr')
		      .append(template);
		      
		      $compile(template)($scope.pvm);
		 });
	};	
	
	//destroy payment template
	m.destroyPaymentTemplate = function(){		
		if($scope.pvm){
			$scope.pvm.$destroy();
		}
		angular.element('#paymentCtrlr').empty();
		
		//Destroy paymentForm and related 'm' fields
		try{if(m.paymentForm) m.paymentForm.destroy();}catch(e){console.log(e);}
		m.cardData = null;
		m.ccErrors = '';
		
		//Destroy review as well;
		m.destroyReviewTemplate();
	};
		
	
	
	
	
	//Load review template
	m.loadReviewTemplate = function(){
		
		$scope.rvm = $scope.$new();
				
		$templateRequest("/resources/ng-templates/cart/review.html?" + cmcVersion)
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('#reviewCtrlr')
		      .append(template);
		      
		      $compile(template)($scope.rvm);
		      
		      /* Scroll down a bit, so customer can see next step*/
		      $('html,body').animate({scrollTop:$(window).scrollTop() + 250},500);
		 });
	};
	
	//destroy review template
	m.destroyReviewTemplate = function(){		
		if($scope.rvm){
			$scope.rvm.$destroy();
		}
		angular.element('#reviewCtrlr').empty();
	};
	
};