//generic pop-up
var _d2dMaskZindex = 9999,
_lbPNotices = false,
_lbUSStates = [{"name":"Alabama","code":"AL"},{"name":"Alaska","code":"AK"},{"name":"American Samoa","code":"AS"},{"name":"Arizona","code":"AZ"},{"name":"Arkansas","code":"AR"},{"name":"California","code":"CA"},{"name":"Colorado","code":"CO"},{"name":"Connecticut","code":"CT"},{"name":"Delaware","code":"DE"},{"name":"District Of Columbia","code":"DC"},{"name":"Federated States Of Micronesia","code":"FM"},{"name":"Florida","code":"FL"},{"name":"Georgia","code":"GA"},{"name":"Guam","code":"GU"},{"name":"Hawaii","code":"HI"},{"name":"Idaho","code":"ID"},{"name":"Illinois","code":"IL"},{"name":"Indiana","code":"IN"},{"name":"Iowa","code":"IA"},{"name":"Kansas","code":"KS"},{"name":"Kentucky","code":"KY"},{"name":"Louisiana","code":"LA"},{"name":"Maine","code":"ME"},{"name":"Marshall Islands","code":"MH"},{"name":"Maryland","code":"MD"},{"name":"Massachusetts","code":"MA"},{"name":"Michigan","code":"MI"},{"name":"Minnesota","code":"MN"},{"name":"Mississippi","code":"MS"},{"name":"Missouri","code":"MO"},{"name":"Montana","code":"MT"},{"name":"Nebraska","code":"NE"},{"name":"Nevada","code":"NV"},{"name":"New Hampshire","code":"NH"},{"name":"New Jersey","code":"NJ"},{"name":"New Mexico","code":"NM"},{"name":"New York","code":"NY"},{"name":"North Carolina","code":"NC"},{"name":"North Dakota","code":"ND"},{"name":"Northern Mariana Islands","code":"MP"},{"name":"Ohio","code":"OH"},{"name":"Oklahoma","code":"OK"},{"name":"Oregon","code":"OR"},{"name":"Palau","code":"PW"},{"name":"Pennsylvania","code":"PA"},{"name":"Puerto Rico","code":"PR"},{"name":"Rhode Island","code":"RI"},{"name":"South Carolina","code":"SC"},{"name":"South Dakota","code":"SD"},{"name":"Tennessee","code":"TN"},{"name":"Texas","code":"TX"},{"name":"Utah","code":"UT"},{"name":"Vermont","code":"VT"},{"name":"Virgin Islands","code":"VI"},{"name":"Virginia","code":"VA"},{"name":"Washington","code":"WA"},{"name":"West Virginia","code":"WV"},{"name":"Wisconsin","code":"WI"},{"name":"Wyoming","code":"WY"}],
_lbStackBottomRight = {"dir1": "up", "dir2": "left", "firstpos1": 25, "firstpos2": 25},
_lbFns = {
	showBusy : function(){
		return $('<div class="fix lboverlay" style="z-index:'+(_d2dMaskZindex++)+'"><span class="lb_busy_pop abs">Processing...</span></div>').appendTo('body');
	},
	
	showViewForm : function(){
		$('.view-edit-form').css('background-color','#ffc').delay(2000).queue(function(){ // Providing a subtle color change for users. 
			$(this).css('background-color','transparent');
			$(this).dequeue();
		});
	},
	
	navOffsetCorrection : function(){
		/*if($('.header-offset').size()!=0){
			$('.header-offset').css('margin-top',(($('.header-navbar').height() + $('.header-sub').height())+'px'));
		}*/
	},
	
	showToTop : function(){
		if($(window).scrollTop()>100){ 
			if($('.to-top').is(':hidden')){
				$('.to-top').fadeIn(300);
			}
		
		} else {
			if($('.to-top').is(':visible')){
				$('.to-top').fadeOut(300);
			}
		}
	},
	
	goToTop : function(){
		$("html, body").animate({'scrollTop':0},200);
		return false;
	},
	
	ratingFunction : function(){
        if($(this).is(':checked')){
            $(this).parent().addClass('checked')
            	.siblings().removeClass('checked');
        }
		return false;
	},
	
	pAlert:function(msg,title,callBack,showCancel){
		var options={title:'Oops!',text:'There was some error processing your request. Please try later.',type:'error',buttons:{sticker:false},hide:false,addclass:'pnotifyNoCancel'};
		if(msg) options.text=msg;
		if(title) options.title=title;
		if(callBack){
			options.buttons.closer=false;
			if(showCancel) options.addclass='';
			options.confirm={confirm:true,align:'center',buttons:[{text:'OK',click:function(n){callBack(n);}},{addClass:'btnCancel'}]};
		}
		new PNotify(options);
	},
	
	pWarn : function(msg,title,callBack){
		if(_lbPNotices){_lbPNotices.remove();}
		var options={title:'Are you sure?',text:'You want to proceed.',type:'notice',buttons:{sticker:false,closer:false},hide:false,addclass:'pnotifyWarn'};
		if(msg) options.text=msg;
		if(title) options.title=title;
		options.confirm={confirm:true,align:'center',buttons:[{text:'Yes',click:function(n){n.remove();callBack(true);}},{text:'No',addClass:'lmarg3',click:function(n){n.remove();callBack(false);}}]};
		_lbPNotices=new PNotify(options);
	},
	
	pInfo : function(msg,delay,bottom){
		var options={text:'Success',type:'info',buttons:{sticker:false}};
		if(delay===undefined || delay===null) options.delay=5000;
		else if(delay===0) options.hide=false;
		else options.delay=delay;
		if(msg) options.text=msg;
		if(bottom){options.addclass="stack-bottomright";options.stack=_lbStackBottomRight;}
		new PNotify(options);
	},
	
	pSuccess : function(msg,delay,bottom){
		var options={text:'Success',type:'success',buttons:{sticker:false}};
		if(delay===undefined || delay===null) options.delay=5000;
		else if(delay===0) options.hide=false;
		else options.delay=delay;
		if(msg) options.text=msg;
		if(bottom){options.addclass="stack-bottomright";options.stack=_lbStackBottomRight;}
		new PNotify(options);
	}
},
_lbUrls = {
	'addtocart' : '/cart/add',	
	'getprd' : '/products/json/',	
	'allprds' : '/products/json/prod-cat',
	'catprds' : '/category/listproducts/',
	'getcartcount' : '/cart/getCount',
	'getcart' : '/cart/getcart',
	'cart' : '/cart/',
	'login' : '/login',
	'register' : '/register',
	'creset' : '/createreset',
	'rsavep' : '/reset/savep'
};

$(document).ready(function(){
	_lbFns.navOffsetCorrection();
	
	$(window)
	.scroll(_lbFns.showToTop)
	.resize(_lbFns.navOffsetCorrection);
	
	$('body')
	.on('click','.to-top',_lbFns.goToTop)
    .on('change', '.rate input', _lbFns.ratingFunction);
	
	
	
	PNotify.prototype.options.styling = "fontawesome";
});