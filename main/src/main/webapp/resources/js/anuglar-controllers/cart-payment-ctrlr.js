var cartPaymentCtrlr = function($scope, $http, $rootScope, $timeout){

	var m = $scope.m,
	pmtApiInit = false;
	
	$scope.nonceRequesting = false;
	$scope.pmtChangable = true;
	$scope.pmtMethod = null;
	$scope.reviewLoaded = false;
	
	/** Payment form related fns **/
	m.cardNonceResponseReceived = function(errors, nonce, cardData) {
		
		if (errors) {			
			m.ccErrors = '';
			
			errors.forEach(function(error) {	
    			m.ccErrors+= error.message;
			});
			
		} else {			
			m.cardData = cardData;
			m.cardData.nonce = nonce;
			//console.log(m.cardData);
			
			$scope.proceedToReview();
		}
		
		$scope.nonceRequesting = false;
		try{$scope.$apply();}catch(err){}		
	};
	
	m.unsupportedBrowserDetected = function() {
		m.ccErrors = 'Sorry, Your browser doesn\'t support ' +
			'the secure processing of this credit card. Please ' +
			'try a different browser.';
		
		$scope.nonceRequesting = false;
		try{$scope.$apply();}catch(err){}
	};
	
	m.paymentForm = new SqPaymentForm({
		
		    applicationId: 'sq0idp-uKKHToPW2VxvmD6WKutvHA',
		    inputClass: 'form-control',
		    inputStyles: [
		      {
		        fontSize: '14px',
		        color: '#555'
		      }
		    ],
		    cardNumber: {
		      elementId: 'sq-card-number',
		      placeholder: '---- ---- ---- ----'
		    },
		    cvv: {
		      elementId: 'sq-cvv',
		      placeholder: 'CVV'
		    },
		    expirationDate: {
		      elementId: 'sq-expiration-date',
		      placeholder: 'MM/YY'
		    },
		    postalCode: {
		      elementId: 'sq-postal-code'
		    },
		    
		    callbacks: {
		    	cardNonceResponseReceived: m.cardNonceResponseReceived,		      
		    	unsupportedBrowserDetected: m.unsupportedBrowserDetected,
		      
		    	inputEventReceived: function(inputEvent) {
		    		switch (inputEvent.eventType) {
		    		
		    		case 'focusClassAdded':
		            // Handle as desired
		    			break;
		    		case 'focusClassRemoved':
		            // Handle as desired
		    			break;
		    		case 'errorClassAdded':
		            // Handle as desired
		    			break;
		    		case 'errorClassRemoved':
		            // Handle as desired
		    			break;
		    		case 'cardBrandChanged':
		            // Handle as desired
		    			break;
		    		case 'postalCodeChanged':
		            // Handle as desired
		    			break;
		    		}
		    	}
		    }
	 });
	
	 $scope.reqCardNonce = function() {	
		 m.ccErrors = '';
		 m.cardData = null;
		 $scope.nonceRequesting = true;
		 m.paymentForm.requestCardNonce();
	 };
	 /** Payment form related fns end **/
	 
	 

	$scope.squareUpInit = function(){
		if(!pmtApiInit) {			
			try{m.paymentForm.build();}catch(e){console.log(e);}
			
			$timeout(function(){
				m.paymentForm.setPostalCode(m.order.shipping.address.zip);
			}, 1000);
			
			pmtApiInit = true;
		}	
	};
	
	
	$scope.proceedToReview = function(){
		if($scope.pmtMethod == 'cod')
			m.pmtCOD = true;
		else
			m.pmtCOD = false;
		
		/* UPDATE THE PAYMENT PARAMS */
		if(!m.order.billing) m.order.billing = {};
		if(!m.order.billing.pmtMethod) m.order.billing.pmtMethod = {};
		
		m.order.billing.pmtMethod.method = $scope.pmtMethod;
		if($scope.pmtMethod=='cc')
			m.order.billing.pmtMethod.type = 'Credit Card';
		
		else if($scope.pmtMethod=='cod')
			m.order.billing.pmtMethod.type = 'Donation on Delivery';
		
		m.order.billing.pmtMethod.cardData = null;
		
		if(m.cardData){				
			m.order.billing.pmtMethod.cardData = m.cardData;
		}		
		/* UPDATE THE PAYMENT PARAMS ENDS */
		
		m.loadReviewTemplate();
		$scope.reviewLoaded = true;
	};
	
	
	$scope.clearReview = function(){
		m.destroyReviewTemplate();
		$scope.reviewLoaded = false;
	};
	 
	
	/* Pmt init function */
	(function(){
		if(!m.codEnabled) {
			$scope.pmtMethod = 'cc';
			$scope.pmtChangable = false;
			$scope.squareUpInit();
		}
		
		else if(!m.ccPmtEnabled) {
			$scope.pmtMethod = 'cod';
			$scope.pmtChangable = false;
		}
	})();
};