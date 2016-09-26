var ModalMarkSoldCtrlr = function ($scope, $uibModalInstance, ms, $http, $filter){

	$scope.ms = ms;
	
	$scope.packetId = 0;
	$scope.packetCode = '';
	$scope.sellingPrice = '';
	$scope.alertTxt = '';
	$scope.dateFinished = '';
	$scope.soldPackets = [];
	$scope.packets = [];
	$scope.totalPrice = 0;
	$scope.totalDiscount = 0;
	
	$scope.pmtModes = _globalPmtModes;
	$scope.pmtModeSelected = $scope.pmtModes[0];
	
	$scope.getPackets = function(val) {		
		return $http.get('/inventory/apps/listpackets', {
			params: {
				s : -1,/**Sales ID is sent as -1 to hide sold packets*/
				pc: val
			}
		}).then(function(response){
			return response.data.result.map(function(item){
				return item.packetCode;
			});
		});
	};
	
	$scope.packetCodeChange=function(callAddPacket){
		if(!$scope.packetCode || $scope.packetCode.length<6) return;
		
		$scope.alertTxt='';
		$scope.sellingPrice = '';
		$http.get('/inventory/apps/packetinfo?pc='+$scope.packetCode).success(function(data){
			if(data.success){
				$scope.sellingPrice = data.packet.markedPrice;
				$scope.packetId = data.packet.id;
				
				if(callAddPacket===true)
					$scope.addPacket();
			}
			else{
				$scope.alertTxt=data.message;
			}
		});
	};
	
	$scope.miscItems = [];
	$scope.showMiscAdd = false;
	$scope.addMiscItem = function(){
		$scope.itemDesc='';
		$scope.itemPrice='';
		$scope.showMiscAdd = true;
	};

	$scope.saveMiscItem = function(){
		if($scope.itemDesc==''){
			$scope.alertTxt='Please enter a brief desciption for the miscellaneous item';
			return;
		}
		
		$scope.itemPrice = $scope.itemPrice.replace('$','').replace(',','');
		if($scope.itemPrice == '' || isNaN(parseFloat($scope.itemPrice))){
			$scope.alertTxt='Please enter valid price';
			return;
		}
		
		$scope.miscItems.push({
			itemDesc : $scope.itemDesc,
			itemPrice : $scope.itemPrice
		});
		

		$scope.itemDesc='';
		$scope.itemPrice='';
		$scope.showMiscAdd = false;		

		/** Find Total*/
		$scope.findTotal();
	};
	
	
	/* Add Discount Buttons */
	$scope.addPromo = function(desc, val){
		$scope.miscItems.push({itemDesc : desc,	itemPrice : val});

		/** Find Total*/
		$scope.findTotal();		
	};
	
	$scope.addCrtsyPromo = function(desc, val){
		$scope.itemDesc='PROMOCRTSY';
		$scope.itemPrice='-10';
		$scope.showMiscAdd = true;	
	};
	
	
	$scope.removeItem = function () {	
		for(var i=0;i<$scope.miscItems.length;i++){
			if($scope.miscItems[i].itemDesc==this.item.itemDesc &&
					$scope.miscItems[i].itemPrice==this.item.itemPrice){
				
				$scope.miscItems.splice(i,1);

				/** Find Total*/
				$scope.findTotal();
				
				break;
			}
		}
	};
	
	$scope.addPacket = function() {	
		/**
		 * Check if the packet is valid and we received its info.
		 * Invalid packet situation happens when packetCodeChange 
		 * is not called for this packet. So we don't have the 
		 * packetId or selling price. Here we try to get it again,
		 * if not found, we will send an error back.
		 **/		
		if($scope.packetId==0 || $scope.sellingPrice == ''){		
			$scope.packetCodeChange(true);
			return;
		}
		
		/**
		 * Check if the packet is already in the soldPackets array.
		 * If yes, throw alert.
		 **/		
		for(var i=0;i<$scope.soldPackets.length;i++){
			if($scope.soldPackets[i].id==$scope.packetId){
				$scope.alertTxt = 'This packet has already been added.'
				break;
			}
		}
		
		if($scope.alertTxt!='') return false;
		
		
		$scope.soldPackets.push({
			id:$scope.packetId,
			packetCode:$scope.packetCode,
			sellingPrice:$scope.sellingPrice			
		});
		
		$scope.packetId = 0;
		$scope.packetCode = '';
		$scope.sellingPrice = '';
		
		/** Find Total*/
		$scope.findTotal();
	};
	
	$scope.removePacket = function () {	
		for(var i=0;i<$scope.soldPackets.length;i++){
			if($scope.soldPackets[i].id==this.packet.id){
				$scope.soldPackets.splice(i,1);

				/** Find Total*/
				$scope.findTotal();
				
				break;
			}
		}
	};
	
	$scope.sellingPriceFocus = function(){
		if($scope.sellingPrice==''){
			$scope.packetCodeChange(false);
		}
	};
	
	$scope.findTotal = function () {	
		var totalPrice = 0;
		for(var i=0;i<$scope.soldPackets.length;i++){
			totalPrice+= parseFloat($scope.soldPackets[i].sellingPrice);
		}
		
		for(var i=0;i<$scope.miscItems.length;i++){
			totalPrice+= parseFloat($scope.miscItems[i].itemPrice);
		}
		
		if($scope.ms.tip){
			totalPrice+= parseFloat($scope.ms.tip);
		}
		
		$scope.totalPrice = totalPrice;
	};
	
	$scope.saveSold=function(){
		
		var totalPackets = $scope.soldPackets.length;
		
		if(totalPackets>0 || $scope.miscItems.length>0){	
			$scope.ms.paymentMode = $scope.pmtModeSelected.value;			
			
			$.post('/inventory/apps/updatedispatch', {id:$scope.ms.itemId, 
						mode:'sold', pmtmode:$scope.pmtModeSelected.value, 
						discount:$scope.totalDiscount,
						tip:$scope.ms.tip,
						split:$scope.ms.splitDetails,
						sps:JSON.stringify($scope.soldPackets), 
						mis:JSON.stringify($scope.miscItems), 
						opsid:_luvbriteGlobalOpsId},
						
				function(data){
							
					if(data.success) {						
						$http.post('/admin/order/json/savestatus', {'_id' : $scope.ms.order._id, 'status' : 'delivered' }).
						then(function(resp){
							var data = resp.data;
							if(data.success){
								$uibModalInstance.close($scope.ms);								
							}
							else{
								alert(data.message);
							}
							
						});
					}
					else {
						alert(data.message);
					}
				}
			);
		}
	};

	$scope.cancel = function () {
	   $uibModalInstance.dismiss('cancel');
	};
	
	$('.packetID').focus();
	
	$scope.checkPromos = function(){
		var o = $scope.ms.order;
		if(o.lineItems && o.lineItems.length){
			o.lineItems.forEach(function(li){
				
				if(li.type == 'coupon'){
					$scope.addPromo('PROMOCPN', -1*li.price);
				}
				
				else if(li.type == 'item' && li.promo){
					
					if(li.promo == 'doubledownoffer'){
						$scope.addPromo('PROMODBLDN', -1*li.cost);
					}
					else if(li.promo == 'offhourpromo'){
						$scope.addPromo('PROMOKIVA', -1*li.cost);
						
					}
					else if(li.promo == 'firsttimepatient'){
						//$scope.addPromo('PROMOCPN', -1*li.cost);						
					}					
				}
				
			});
			
		}
		
	};
	$scope.checkPromos();
};