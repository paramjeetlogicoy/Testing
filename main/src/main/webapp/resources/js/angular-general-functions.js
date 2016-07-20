/**
 * Generic functions used in all angular JSs are defined here.
 **/

var appRunFns = function($http, $rootScope, $interval){
	
	//Set generic messages
	$rootScope.errMsgPageRefresh = "There was some error processing your request." 
		+ " Please refresh the page and try again.";
	
	//Set generic messages
	$rootScope.errMsgContactSupport = "There was some error processing your request." 
		+ " Please refresh the page and try again. If problem persists, please contact support.";
	
	//Add X-Requested-With to headers to detect Ajax calls
	$http.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
	
	//provide an overlay during Ajax operations    
    $rootScope.$on('ajax-start',function(){
    	_lbFns.showBusy();
    });
    
    $rootScope.$on('ajax-stop',function(){
    	$('div.lboverlay').remove();
    });
    
    
    //Set the CDN path
    $rootScope.lbGlobalCDNPath = _lbGlobalCDNPath; /*Global var defined in <head />*/
    
    
    //Disclaimer
    var cookieVal = $.cookie('above21');
    if(!cookieVal && $('#noLbDisclaimer').size()==0){
    	$('.lb-disclaimer-sm').modal();
    }
    $rootScope.saveDisclaimerSession = function(){
    	$.cookie('above21','accepted',{expires:30, path:'/'});
    	$('.lb-disclaimer-sm').modal('hide');
    }
    
    
    //Correct the csrf param periodically.
    var rootPrevValue = $('#_lb_sec_tok').val();
    csrfCorrection = $interval(function(){
    	$http.get('/check?hidepop')
    	.then(function(resp){
    		var x = resp.data;
    		$('#_lb_sec_tok').val(x.message);    		
    		if(rootPrevValue != x.message){
    			$.cookie('XSRF-TOKEN', x.message, {path:'/'});
    			rootPrevValue = x.message;
    		}
    	},
    	function(){
    		if (angular.isDefined(csrfCorrection)) {
                $interval.cancel(csrfCorrection);
    		}
    	});
    }, 5*60*1000);
    
    
    //initialize $rootScope.rootCartCount
    $rootScope.rootCartCount = 0;
    $rootScope.$on('updateCartCount',function(){
        $http.get(_lbUrls.getcartcount, {
    		params : { 
    			'hidepop' : true  //tells the config not to show the loading popup
    		}
    	})
    	.success(function(data){
    		$rootScope.rootCartCount = data.cartCount;
    	})    	
    });
    
    if($('.header-cart').size()>0)
    	$rootScope.$broadcast("updateCartCount");//First time execute;
    
    
    $rootScope.formElemValidate = function(event){
    	var $elm = angular.element(event.target),
    		y = $elm.data('$ngModelController');
    	
    	if(y){
    		if(y.$invalid && !y.$pristine)
    			$elm.parents('.form-group').addClass('has-error');
    		else{
    			$elm.parents('.form-group').removeClass('has-error');
    		}
    	}
    };
    
/*    $rootScope.$on('$locationChangeStart', function (event, next, current) {
    	console.log("Location change triggered. - " + next + " - " + current);
    });*/
},

appConfigs = function($httpProvider){
    $httpProvider.interceptors.push(function ($q, $rootScope) {
        var requests = 0;
 
        function show(config) {
            if (!requests) {
            	try {
            		if(config 
            				&& (config.url.indexOf('/upload') > -1
            					|| config.url.indexOf('hidepop') > -1
            					|| config.params.hidepop)){ //No overlay for uploads
            		}
            		else {
            			$rootScope.$broadcast("ajax-start");
            		}
            	}catch(e){
            		$rootScope.$broadcast("ajax-start");
            	}
            }
            requests++;
        }
 
        function hide() {
            requests--;
            if (!requests) {
                $rootScope.$broadcast("ajax-stop");
            }
        }
 
        return {
            'request': function (config) {
                show(config);
                return $q.when(config);
            }, 
            
            'response': function (response) {
                hide();
                return $q.when(response);
            }, 
            
            'responseError': function (rejection) {
                hide();
                return $q.reject(rejection);
            }
        };
    });
};