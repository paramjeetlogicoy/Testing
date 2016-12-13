var cartReviewCtrlr = function($scope, $http, $rootScope){
	
	var m = $scope.m;
	
	$scope.reviewErrors = '';	
	
	$scope.placeOrder = function(){
		
		if(m.order._id && m.user._id){	
			
			m.ccErrors = '';
			$scope.reviewErrors = '';
			
			$http
			.post(_lbUrls.cart + m.order._id + '/placeorder', m.order)
			.then(
					function(resp){
						if(resp && resp.data){
							if(resp.data.success){
								location.href = "/confirmation/" + resp.data.message;
							}
							else{
								
								if(!resp.data.paymentProcessed && resp.data.paymentError !== ''){
									
									if(resp.data.message == 'retry'){
										m.ccErrors = 'Payment request timed out, please try again.';
									}
									else{
										m.ccErrors = resp.data.paymentError;
									}

									m.cardData = '';
									m.destroyReviewTemplate();
								}
								else{
									
									if(resp.data.paymentProcessed && resp.data.orderFinalizationError){
										$scope.reviewErrors = 'Your payment was processed successfully. ' +
											'But there was some error finializing the order. ' +
											'Please don\'t place the order again. ' +
											'Please call our customer care. Error details -  '+ resp.data.message;
									}
									
									else{
										$scope.reviewErrors = resp.data.message;										
									}
									
								}
							}
						}
						else{
							$scope.reviewErrors = 'There was error placing the order. ' + 
								'Please refresh the page and try again later. ' + 
								'If problem persists, please contact customer care.';
						}
					},
					function(){
						$scope.reviewErrors = $rootScope.errMsgPageRefresh;
					}
			);			
		}
		else{
			$scope.reviewErrors = 'Unable to find an order. ' +
				'Are you sure you have items in your cart and ' +
				'you are logged in to your account?';
		}
		
	};
};