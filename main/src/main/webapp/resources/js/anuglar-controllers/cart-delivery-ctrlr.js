var cartDeliveryCtrlr = function($scope, $http, $rootScope, $filter, $timeout){

	var m = $scope.m,
	addressApiInit = false,
	
	getAddress = function(){
		return m.order.shipping.address.address1 + ' ' + m.order.shipping.address.city + ' ' + 
				m.order.shipping.address.state + ' ' + m.order.shipping.address.zip;
	},	
	
	getAddressMapLink = function(){
		if(m.order.shipping && m.order.shipping.address) {
			var mapLink = {
					'markers':'color:blue|label:S|' + getAddress(),
					'zoom':17,
					'scale':2,
					'size':'280x280',
					'center':getAddress(),
					'key':'AIzaSyDOOuxNzzE247y4HbG9B5J2yM8vzzhegCU'};
			
			
			return "https://maps.googleapis.com/maps/api/staticmap?" +
						$.param(mapLink);
		}
		else
			return null;
	},
	
	addressLogic = function(){
		
		if(m.order.shipping.address && (m.order.shipping.address.address1 !== '' ||
			m.order.shipping.address.city !== '' ||
			m.order.shipping.address.state !== '' ||
			m.order.shipping.address.zip !== '')){			 
			
			$scope.mainZip = m.order.shipping.address.zip;
			$scope.validateZip(true);
		}
	},
	
	proceedToPayment = function(){
		
		m.addressSaved = true; 
		
		$('.delivery-address').empty()
		.append($('<img />').attr({'src' : getAddressMapLink(), 'class':'fit'}));
		
/*		if($scope.shippingSelected) 
			m.codEnabled = false;
		else*/
			m.codEnabled = true;
		
		if(m.user && m.user._id)
			m.loadPaymentTemplate();
	},
	
	setUpDeliveryTimes = function(){
		
		if(m.emptyCart){
			console.log('setUpDeliveryTimes cancelled');
			return;
		}
		
		var d = new Date(),
		currHour = d.getHours(),
		timeTocheck = 1000 * 60 * 60; //default every hour	
		
		if(currHour >= 23){ //if past 11PM
			//delivered tomorrow after 11 AM;
			d.setDate(d.getDate()+1);
			
			$scope.deliveryTimeText = 'Tomorrow - ' +
				$filter('date')(d, 'mediumDate') +
				' after 11:00 AM PST';			
			
			$scope.endTime = null;		
		}  
		
		else if(currHour < 11){//if before 11AM, delivered today after 11 AM;
			
			if(currHour > 10){//After 10 AM			
				if(d.getMinutes() < 30){
					//start checking every 15 minute.
					timeTocheck = 1000 * 60 * 15;
				}
				else {
					//start checking every minute.
					timeTocheck = 1000 * 60;
				}
			}
			
			
			$scope.deliveryTimeText = 'Today - ' +
				$filter('date')(d, 'mediumDate') + 
				' after 11:00 AM PST';		
			
			$scope.endTime = null;
		}
		
		else if(currHour > 20){//During our operational period
			
			if(d.getMinutes() < 30){
				//start checking every 15 minute.
				timeTocheck = 1000 * 60 * 15;
			}
			else {
				//start checking every minute.
				timeTocheck = 1000 * 60;
			}
			
			
			d.setHours(22,59,0);			
			$scope.endTime = d;
			
			$scope.deliveryTimeText = 'Today - ' +
				$filter('date')(d, 'mediumDate');
		}
		else{
			//delivered today
			
			$scope.deliveryTimeText = 'Today - ' + 
				$filter('date')(d, 'mediumDate');		
			
			$scope.endTime = null;
		}
		
		$timeout(setUpDeliveryTimes, timeTocheck);
	},
	
	initAddressVars = function(){
		if(!m.order.shipping) m.order.shipping = {};		
		if(!m.order.shipping.address) m.order.shipping.address = {};
	};	
	

	$scope.addressError = '';
	$scope.deliveryOptionSelected = true; //false;
	$scope.deliveryEligible = false;
	$scope.shippingEligible = false;
	$scope.deliverySelected = true; //false;
	$scope.shippingSelected = false;
		
	$scope.deliveryTimeText = '';
	$scope.endTime = null;
	
	
	

	$scope.resetDeliveryOptions = function(){
/*		$scope.deliveryEligible = false;
		$scope.shippingEligible = false;
		
		$scope.deliverySelected = false;
		$scope.shippingSelected = false;

		$scope.deliveryOptionSelected = false;*/
		
		$scope.addressError = '';
		$scope.invalidZip = '';
		
		m.addressSaved = false;
		
		/** Destroy payment form is present **/
		m.destroyPaymentTemplate();
	};	
	
	
	$scope.checkDeliveryOptions = function(){
		$scope.validateZip();
		
		if(m.order.shipping && m.order.shipping.address){
			m.order.shipping.address.zip = $scope.mainZip;
		}
	};
	
	$scope.invalidZip = '';
	$scope.validateZip = function(auto){
		
		$scope.resetDeliveryOptions();
		
		$http.get(_lbUrls.cart + '/validatezip',{
			params : { 
				'zip' : $scope.mainZip
			}
		})
		.then(
				function(resp){
					if(resp.data && resp.data.success){
						if(resp.data.message == "both"){
							$scope.deliveryEligible = true;
							$scope.shippingEligible = true;
							
							//If both option available, select delivery in auto mode.
							if(auto){
								$scope.deliverySelected = true;
								$scope.selectDeliveryOption();
							}
						}
						else if(resp.data.message == "local"){
							$scope.deliveryEligible = true;		
							
							if(auto){
								$scope.deliverySelected = true;
								$scope.selectDeliveryOption();
							}
						}
						else if(resp.data.message == "shipping"){
							$scope.shippingEligible = true;		
							
							if(auto){
								$scope.shippingSelected = true;
								$scope.selectDeliveryOption();
							}					
						}
						else if(!auto){
							$scope.invalidZip = "Sorry, we currently don't service your area. " + 
								"We are working very hard on expanding to your city. ";								
						}
					}
				},
				
				function(){
					$scope.invalidZip = $rootScope.errMsgPageRefresh;
				}
		);
	};
	
	
	$scope.selectDeliveryOption = function(){
		$scope.deliveryOptionSelected = true;

		if(!addressApiInit)
			$scope.mapsInit();
	};
	
	
	$scope.saveDeliveryAddress = function(){
		
		if($scope.cartAddress.$valid){
			$scope.addressError = '';
			
			if($scope.deliverySelected) 
				m.order.shipping.deliveryMethod = 'Local Delivery';
			
			else if($scope.shippingSelected)
				m.order.shipping.deliveryMethod = 'Overnight Shipping';
			
			$http.post(_lbUrls.cart + m.order._id + '/savedeliveryaddr', m.order.shipping)
			.then(
					function(resp){
						if(resp.data && resp.data.success){
							_lbFns.pSuccess('Address Saved.');
							
							if(resp.data.message){
								m.order.shipping.deliveryMethod = resp.data.message;
								
								if(resp.data.message == 'Local Delivery'){
									$scope.deliverySelected = true;
									$scope.shippingSelected = false;
								}
								
								else if(resp.data.message == 'Overnight Shipping'){
									$scope.shippingSelected = true;
									$scope.deliverySelected = false;
								}
							}
							
							proceedToPayment();
						}
					
						else if(resp.data && resp.data.message){
							$scope.addressError = resp.data.message;
						}
						else{
							$scope.addressError = $rootScope.errMsgPageRefresh;
						}
					},
					
					function(){
						$scope.addressError = $rootScope.errMsgPageRefresh;
					}
			);			
		}		
	};	
	
	
	$scope.editAddress = function(){
		m.addressSaved = false;
		
		$scope.showPayment = false;
		$scope.cartAddress.$setDirty();
		
		if(!addressApiInit)
			$scope.mapsInit();
		
		/** Destroy payment form is present **/
		m.destroyPaymentTemplate();
	};
	
	
	$scope.saveDeliveryNotes = function(){		
		
		if(!m.order.notes) m.order.notes = {};
		if(!m.order.notes.deliveryNotes) m.order.notes.deliveryNotes = '';
		
		if(m.order._id && m.order.notes.deliveryNotes){			
			$http.post(_lbUrls.cart + m.order._id + '/savedeliverynote?hidepop',
				{'deliveryNotes':m.order.notes.deliveryNotes});			
		}		
	};			
	
	
	/* Google MAPS API related Functions */
	$scope.gmapComponentForm = {
	        street_number: 'short_name',
	        route: 'short_name', //Street Address
	        locality: 'long_name',  //city
	        administrative_area_level_1: 'short_name',  //State
	        postal_code: 'short_name'
	};
	
	$scope.geolocate = function() {
		if (navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(function(position) {
				var geolocation = {
						lat: position.coords.latitude,
						lng: position.coords.longitude
				};
				var circle = new google.maps.Circle({
					center: geolocation,
					radius: position.coords.accuracy
				});
				
				$scope.autocomplete.setBounds(circle.getBounds());
			});
		}
	};
	
	$scope.fillInAddress = function(){
		var place = $scope.autocomplete.getPlace(),
		streetNumber = '',
		streetAddr = '';
		
		for (var i = 0; i < place.address_components.length; i++) {
			var addressType = place.address_components[i].types[0];
			if ($scope.gmapComponentForm[addressType]) {
				var val = place.address_components[i][$scope.gmapComponentForm[addressType]];
				
				switch(addressType){
				
					case 'street_number':
						streetNumber = val; 
						break;
						
					case 'route':
						streetAddr = val; 
						break;
					
					case 'locality':
						m.order.shipping.address.city = val; 
						break;
					
					case 'administrative_area_level_1':
						m.order.shipping.address.state = val; 
						break;
					
					case 'postal_code':
						m.order.shipping.address.zip = val; 
						break;
						
					default:
						break;
				}
			}
		}
		
		m.order.shipping.address.address1 = streetNumber? (streetNumber + ' ' + streetAddr) : streetAddr;
		try{$scope.$apply();}catch(err){}
		
		$('#cartAddress .form-control').each(function(){
			$(this).blur();
		});
	};
	
	$scope.mapsInit = function(){
		$scope.autocomplete = new google.maps.places.Autocomplete(
				(document.getElementById('addressLocator')),
	            {
					types: ['geocode'],
					componentRestrictions: {country: 'us'}
				});
		
		$scope.autocomplete.addListener('place_changed', $scope.fillInAddress);
		$scope.geolocate();
		
		initAddressVars();
		
		addressApiInit = true;
	};	
	
	/* Google MAPS API related Functions Ends */
	

	
	/** DELIVERY INIT **/	
	(function(){
		
		/*if there is shipping info in the order, use it*/
		if(m.order && m.order.shipping && m.order.shipping.address){		
			setUpDeliveryTimes();
			addressLogic();
			
			if(!addressApiInit)
				$scope.mapsInit();
		}
		
		else if(m.user && m.prevOrderAddress){		
			setUpDeliveryTimes();
			initAddressVars();
			
			m.order.shipping.address = m.prevOrderAddress;
			
			if(!addressApiInit)
				$scope.mapsInit();
		}
		
		else if(m.user && m.user.billing){		
			setUpDeliveryTimes();
			initAddressVars();
			
			m.order.shipping.address = m.user.billing;
			
			if(!addressApiInit)
				$scope.mapsInit();
		}
	})();
};