var showPopUp = function(event){
	try {
		var leftVal=(event.pageX-120),
		  topVal=event.pageY;
		
		if(leftVal < 0){
			leftVal = 0;
		}
		if(topVal < 0) {
			topVal = 0;
		}
		
		$('#twosubalertmsg').css({left:leftVal+"px",top:topVal+"px"}).show().delay(3000).fadeOut();
		
	}catch(e){
		
	}
}, 

surveyCtrlr = function($scope, $http){
	
	var saveResponse = {"sr":{"_id":null,"sid":1,"date":new Date(),"customer":null,"orderNumber":0,"otherRef":null,
		"details":[
				{"qid":1,"response":"Nose,2|Price,1|Structure,3|Amount,5|Familiarity/Reputability,4","comment":null},
				{"qid":2,"response":"Agree","comment":null},
				{"qid":3,"response":"Flowers,Sativa,Indica,Drinks","comment":null},
				{"qid":4,"response":"Yes","comment":null},
				{"qid":5,"response":"No","comment":"Some reason"},
				{"qid":6,"response":"Yes","comment":null},
				{"qid":7,"response":"Not sure yet","comment":""},
				{"qid":8,"response":"","comment":"General"}
			]
		},
		"sfs":[
			{ "_id":null, "sid":1, "qid":1, "resp":"Nose", "value":2},
			{ "_id":null, "sid":1, "qid":1, "resp":"Price", "value":1},
			{ "_id":null, "sid":1, "qid":1, "resp":"Structure", "value":3},
			{ "_id":null, "sid":1, "qid":1, "resp":"Amount", "value":5},
			{ "_id":null, "sid":1, "qid":1, "resp":"Familiarity/Reputability", "value":4},
			
			{ "_id":null, "sid":1, "qid":2, "resp":"Agree", "value":1},

			{ "_id":null, "sid":1, "qid":3, "resp":"Flowers", "value":1},
			{ "_id":null, "sid":1, "qid":3, "resp":"Sativa", "value":1},
			{ "_id":null, "sid":1, "qid":3, "resp":"Indica", "value":1},
			{ "_id":null, "sid":1, "qid":3, "resp":"Drinks", "value":1},
			
			{ "_id":null, "sid":1, "qid":4, "resp":"Yes", "value":1},
			{ "_id":null, "sid":1, "qid":5, "resp":"No", "value":1},
			{ "_id":null, "sid":1, "qid":6, "resp":"Yes", "value":1},
			{ "_id":null, "sid":1, "qid":7, "resp":"Not sure yet", "value":1}
		]
	},
	
	submitSurvey = function(surveyData){
		
		$http.post("/survey/savesurvey",surveyData)
		.then(function(resp){
			if(resp.data){
				if(resp.data.success){
					$scope.surveyCompleted = true;
				}
				else if(resp.data.message){
					$scope.surveyErrorMsg = resp.data.message;
				}
				else{
					$scope.surveyErrorMsg = "There was some error saving the survey.";
				}
			}
			
		}, function(){
			alert("Sorry there was some error updating the Survey.");
		});
	};
	
	
	
	$scope.takeSurvey = false;
	$scope.surveyCompleted = false;
	$scope.surveyErrorMsg = '';
	
	$scope.surveyOptions = {
			
			"q1" : {
				options: [
					{name:"Nose", value:0},
					{name:"Price", value:0},
					{name:"Structure", value:0},
					{name:"Amount", value:0},
					{name:"Familiarity/Reputability", value:0}
				]
			},
			
			"q2" : {
				options : ["Strongly Agree", "Agree", "Neutral", "Disagree", "Strongly Disagree"],
				selected : ""
			},
	
			"q3" : { 
				options :  [
					{cat : "Flowers", 
						subCat : [{value : "Sativa", checked : false}, {value : "Indica", checked : false}, {value : "Hybrid", checked : false}]
					},
					{cat : "Edibles", 
						subCat : [{value : "Drinks", checked : false}, {value : "Chocolates", checked : false}, 
							{value : "Gummies", checked : false}, {value : "Hard Candies", checked : false}, 
							{value : "Baked Goods (Cookies, pretzels, brownies, etc.)", checked : false}]
					},
					{cat : "Pre-Rolls", 
						subCat : [{value : "Standard", checked : false}, 
							{value : "Concentrated (containing hash/wax, etc.)", checked : false}]
					},
					{cat : "Concentrates", 
						subCat : [{value : "Wax/Shatter", checked : false}, {value : "Hash", checked : false}, 
							{value : "Budder", checked : false}, {value : "Crumble", checked : false}, 
							{value : "Vapes", checked : false}, {value : "Tinctures", checked : false}, {value : "Topicals", checked : false}]
					},
					{cat : "Accessories",
						subCat : [{value : "Pipes", checked : false}, {value : "Bongs", checked : false}, 
							{value : "Lighters", checked : false}, 
							{value : "Rolling Papers (Blunt wraps, Zig Zags, etc.)", checked : false}, 
							{value : "Grinders", checked : false}]
					}
					
				],
				
				selected : {}
			},
			
			"q4" : {
				options : ["Yes", "No"],
				selected : "",
				comments : {
					text : 'If no, please explain why using a brief sentence or two:',
					value : ''
				}
			
			},
			
			"q5" : {
				options : ["Yes", "No"],
				selected : "",
				comments : {
					text : 'If no, please explain why using a brief sentence or two:',
					value : ''
				}
			
			},
			
			"q6" : {
				options : ["Yes", "No", "N/A (ex. this is your first time ordering)"],
				selected : "",
				comments : {
					text : 'If no, please explain why using a brief sentence or two:',
					value : ''
				}
			
			},
			
			"q7" : {
				options : ["Yes", "No", "Not sure yet", "Choose Not to Answer"],
				selected : "",
				comments : {
					text : 'If no, please explain why using a brief sentence or two (Feel free to list where else you commonly purchase from. Don\'t worry, you won\'t hurt our feelings.)',
					value : ''
				}
			
			},
			
			"q8" : {
				comments : ''
			}
	};
	
	
	
	$scope.subCatClick = function($event){
		var curr = this,
		subsChecked = 0;
		if(curr.subcat.checked) { 
			
			$scope.surveyOptions
			.q3.options
			.forEach(function(so){
				if(so != curr.so){
					
					so.subCat.forEach(function(subcat){
						subcat.checked = false;
					});
				}
				
				else {
					
					so.subCat.forEach(function(subcat){
						if(subcat.checked)
							subsChecked++;
					});
					
					if(subsChecked > 2){
						curr.subcat.checked = false;
						showPopUp($event);
					}
				}
			});
			
			
			//if subcat is checked, make sure the category is also checked
			if($scope.surveyOptions.q3.selected != curr.so) {
				$scope.surveyOptions.q3.selected = curr.so;
			}
		}
	};
	
	
	$scope.catClick = function(){
		var curr = this;
			
		$scope.surveyOptions
		.q3.options
		.forEach(function(so){
			if(so != curr.so){
				//Remove checked subCats from other categories!
				so.subCat.forEach(function(subcat){
					subcat.checked = false;
				});
			}
		});
	};
	
	
	$scope.validateSurvey = function(){
		
		var err = false,
			so = $scope.surveyOptions;
		
		so.q1.options.forEach(function(opt){
			if(!opt.value){
				opt.error = true;
				err = true;
			}
		});

		
		if(!so.q2.selected){
			so.q2.error = true;
			err = true;
		}

		
		if(!so.q3.selected.cat){
			so.q3.error = true;
			err = true;
		}

		
		if(!so.q4.selected){
			so.q4.error = true;
			err = true;
		}

		
		if(!so.q5.selected){
			so.q5.error = true;
			err = true;
		}

		
		if(!so.q6.selected){
			so.q6.error = true;
			err = true;
		}

		
		if(!so.q7.selected){
			so.q7.error = true;
			err = true;
		}
		
		if(err) return;
		
		var details = [],
		sfs = [];
		

		var q1Dt = {},
			q1Sfs = {},
			q1Temp = '';
		so.q1.options.forEach(function(opt){
			q1Temp+= ('|' + opt.name + ',' + opt.value);
			
			q1Sfs = { "_id":null, "sid":1, "qid":1, "resp":opt.name, "value":opt.value };
			sfs.push(q1Sfs);
		});
		q1Dt = {"qid":1,"response":q1Temp.substr(1),"comment":null};
		details.push(q1Dt);
		
		
		var q2Dt = {"qid":2,"response":so.q2.selected,"comment":null},
			q2Sfs = { "_id":null, "sid":1, "qid":2, "resp":so.q2.selected, "value":1 };
		details.push(q2Dt);
		sfs.push(q2Sfs);
		
		
		var q3Dt = {},
			q3Sfs = {},
			q3Temp = so.q3.selected.cat;
		
		q3Sfs = { "_id":null, "sid":1, "qid":3, "resp":q3Temp, "value":1 };
		sfs.push(q3Sfs);
		
		so.q3.selected.subCat.forEach(function(opt){
			
			if(opt.checked){
				q3Temp+= (',' + opt.value );
				
				q3Sfs = { "_id":null, "sid":1, "qid":3, "resp":opt.value, "value":1 };
				sfs.push(q3Sfs);
			}
		});
		q3Dt = {"qid":3,"response":q3Temp,"comment":null};
		details.push(q3Dt);
		
		var q4Dt = {"qid":4,"response":so.q4.selected,"comment":so.q4.comments.value},
			q4Sfs = { "_id":null, "sid":1, "qid":4, "resp":so.q4.selected, "value":1 };
		details.push(q4Dt);
		sfs.push(q4Sfs);
		
		
		var q5Dt = {"qid":5,"response":so.q5.selected,"comment":so.q5.comments.value},
			q5Sfs = { "_id":null, "sid":1, "qid":5, "resp":so.q5.selected, "value":1 };
		details.push(q5Dt);
		sfs.push(q5Sfs);
		
		
		var q6Dt = {"qid":6,"response":so.q6.selected,"comment":so.q6.comments.value},
			q6Sfs = { "_id":null, "sid":1, "qid":6, "resp":so.q6.selected, "value":1 };
		details.push(q6Dt);
		sfs.push(q6Sfs);
		
		
		var q7Dt = {"qid":7,"response":so.q7.selected,"comment":so.q7.comments.value},
			q7Sfs = { "_id":null, "sid":1, "qid":7, "resp":so.q7.selected, "value":1 };
		details.push(q7Dt);
		sfs.push(q7Sfs);
		
		
		var q8Dt = {"qid":8,"response":'',"comment":so.q8.comments};
		details.push(q8Dt);
		
		
		var surveyResponse = {
				sr : {
					"_id":null,
					"sid":1,
					"date":new Date(),
					"customer":$('#customerName').val(),
					"orderNumber":$('#orderNumber').val(),
					"otherRef":null,
					
					"details" : details
				},
				
				"sfs" : sfs
		};
		
		console.log(surveyResponse);
		submitSurvey(surveyResponse);

	};
	
};