/**
 * Generic functions used in all angular JSs are defined here.
 **/

var appRunFns = function($http, $rootScope, $interval){
	
	//Set generic messages
	$rootScope.errMsgPageRefresh = "There was some error processing your request." + 
		" Please refresh the page and try again.";
	
	//Set generic messages
	$rootScope.errMsgContactSupport = "There was some error processing your request." + 
		" Please refresh the page and try again. If problem persists, please contact support.";
	
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
    if(!cookieVal && $('#noLbDisclaimer').size()===0){
    	$('.footer-disclaimer').addClass('present');
    }else{
    	$('.footer-disclaimer').remove();
    }
    $rootScope.saveDisclaimerSession = function(){
    	$.cookie('above21','accepted',{expires:30, path:'/'});
    	$('.footer-disclaimer').animate({opacity:0}, 1000, function(){
    		$(this).remove();
    	});
    };
    
    
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
    	});    	
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
    
    //Search Fn    
    $rootScope.gSearch = '';
    $rootScope.showGlobalSearch = function(){
    	$rootScope.gSearch = '';
    	$('.nav-search').slideDown(300, function(){
    		$(this).find('input').focus();
    	});
    };
    $rootScope.hideGlobalSearch = function(){
    	$('.nav-search').fadeOut(300);
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
            		if(config &&
            				(config.url.indexOf('/upload') > -1 || 
            					config.url.indexOf('hidepop') > -1 ||
            					config.params.hidepop)){ //No overlay for uploads
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
},

remainingTime = function($interval) {
	// return the directive link function. (compile function not needed)
	return function(scope, element, attrs) {
		var endTime = 0,
		timeOneDay = 1000 * 60 * 60 * 24,
		stopTime; // so that we can cancel the time updates

		// used to update the UI
		function clearTime() {
			element.text('');
			$interval.cancel(stopTime);
		}

		// used to update the UI
		function updateTime() {
			var now = new Date().getTime(),
			et = endTime.getTime();
			
			if((et - now) > 0){
				var mils = et - now,
					seconds = Math.floor((mils / 1000) % 60),
	            	minutes = Math.floor(((mils / (60000)) % 60)),
	            	hours = Math.floor(((mils / (3600000)) % 24)),
	            	display = '';
				
				if(hours>0) display+= (hours + 'h ');
				
				display+= (minutes + 'm ' + seconds + 's');
				
				element.text(' (If you order in ' + display + ')');
			}
			else {
				element.text('Sorry you are past the cutt off time.');
				$interval.cancel(stopTime);
			}
		}

		// watch the expression, and update the UI on change.
		scope.$watch(attrs.remainingTime, function(value) {
			if(value) {
				endTime = value;
				updateTime();
			}
			else
				clearTime();
		});

		stopTime = $interval(updateTime, 1000);

		// listen on DOM destroy (removal) event, and cancel the next UI update
		// to prevent updating time after the DOM element was removed.
		element.on('$destroy', function() {
			$interval.cancel(stopTime);
		});
	};
};