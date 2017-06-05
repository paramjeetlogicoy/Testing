var lbApp = angular.module(
	'lbApp', [
	     //'ngRoute',
	     'ngAnimate',
         'ngSanitize',
         'ui.bootstrap',
         'ngFileUpload'
     ]
)

//below fns are defined in angular-general-functions.js
.config(['$httpProvider', appConfigs])
.run(['$http', '$rootScope', '$interval', appRunFns]) 
//end fns defined in angular-general-functions.js



//All controllers are implemented as separate functions in the folder 'angular-controllers'

.controller('allProductCtrlr', 			['$scope', '$http', '$rootScope', '$templateRequest', '$compile', 'categoryFltrFilter', allProductCtrlr])
	.controller('allProductPriceCtrlr', ['$scope', '$http', '$rootScope', '$timeout', allProductPriceCtrlr])

.controller('productCtrlr', 		 ['$scope', '$http', '$rootScope', productCtrlr])
.controller('loginCtrlr', 		 	 ['$scope', '$http', loginCtrlr])
.controller('registerCtrlr', 		 ['$scope', '$http', 'Upload', registerCtrlr])
.controller('resetCtlr', 			 ['$scope', '$http', '$rootScope', resetCtlr])
.controller('cartMainCtrlr',         ['$scope', '$http', '$templateRequest', '$compile', '$rootScope', cartMainCtrlr])
	.controller('cartItemCtrlr',     ['$scope', '$http', '$rootScope', '$timeout', cartItemCtrlr])
	.controller('cartDeliveryCtrlr', ['$scope', '$http', '$rootScope', '$filter', '$timeout', cartDeliveryCtrlr])
	.controller('cartPaymentCtrlr',  ['$scope', '$http', '$rootScope','$timeout', cartPaymentCtrlr])
	.controller('cartCouponCtrlr',   ['$scope', '$http', '$rootScope', cartCouponCtrlr])
	.controller('cartReviewCtrlr',   ['$scope', '$http', '$rootScope', cartReviewCtrlr])
	
.controller('localBoxCtrlr', 		 ['$scope', '$http', '$rootScope', localBoxCtrlr])
.controller('homeCtrlr', 			 ['$scope', '$http', '$rootScope', homeCtrlr])
.controller('acctExpiredCtrlr', 	 ['$scope', '$http', 'Upload', acctExpiredCtrlr])
.controller('surveyCtrlr', 	 		 ['$scope', '$http', surveyCtrlr])

// This directive is implemented in angular-general-functions.js
.directive('remainingTime', ['$interval', remainingTime])

.filter('categoryFltr', categoryFilter);