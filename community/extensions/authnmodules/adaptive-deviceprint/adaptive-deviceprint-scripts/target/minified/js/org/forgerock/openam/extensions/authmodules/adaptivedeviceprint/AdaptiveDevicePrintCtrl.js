define("org/forgerock/openam/extensions/authmodules/adaptivedeviceprint/AdaptiveDevicePrintCtrl",["org/forgerock/openam/extensions/authmodules/adaptivedeviceprint/AdaptiveDevicePrintView","org/forgerock/openam/extensions/authmodules/adaptivedeviceprint/DevicePrintInfoAggregator","org/forgerock/openam/extensions/authmodules/adaptivedeviceprint/EventManager","org/forgerock/openam/extensions/authmodules/adaptivedeviceprint/Constants"],function(a,b,c,d){var e={};return e.view=a,e.init=function(){e.fillInputWithDevicePrintInfo(),c.registerListener(d.EVENT_RECOLLECT_DATA,e.fillInputWithDevicePrintInfo)},e.fillInputWithDevicePrintInfo=function(){e.view.getLoginForm().size()===0&&setTimeout(e.fillInputWithDevicePrintInfo,100);var a=b.collectInfo();console.log("collected info: "+a),e.view.getDevicePrintInfoInput().val(JSON.stringify(a))},e})