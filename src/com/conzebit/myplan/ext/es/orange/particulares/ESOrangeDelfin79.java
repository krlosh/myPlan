package com.conzebit.myplan.ext.es.orange.particulares;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.conzebit.myplan.core.Chargeable;
import com.conzebit.myplan.core.call.Call;

import com.conzebit.myplan.core.message.ChargeableMessage;
import com.conzebit.myplan.core.msisdn.MsisdnType;
import com.conzebit.myplan.core.plan.PlanChargeable;
import com.conzebit.myplan.core.plan.PlanSummary;
import com.conzebit.myplan.core.sms.Sms;
import com.conzebit.myplan.ext.es.orange.ESOrange;

/**
 * Delfín 79.
 * 
 * @author sanz
 */
public class ESOrangeDelfin79 extends ESOrange {
    
	private double monthFee = 79;
	private double initialPrice = 0.15;
	private double pricePerSecond = 0.18 / 60;
	private double smsPrice = 0.15;
	private int maxRecipients = 75;
	
	private int maxSecondsMonth = 2000 * 60;
	private int maxSmsMonth = 500;
	
    
	public String getPlanName() {
		return "Delfín 79";
	}

	public String getPlanURL() {
		return "http://movil.orange.es/tarifas-y-ahorro/contrato/delfin-79/";
	}

	public PlanSummary process(ArrayList<Chargeable> data) {
		PlanSummary ret = new PlanSummary(this);
		ret.addPlanCall(new PlanChargeable(new ChargeableMessage(ChargeableMessage.MESSAGE_MONTH_FEE), monthFee, this.getCurrency()));

		int secondsTotal = 0;
		int smsTotal = 0;
		Set<String> recipients = new HashSet<String>();
		for (Chargeable chargeable : data) {
			if (chargeable.getChargeableType() == Chargeable.CHARGEABLE_TYPE_CALL) {
				Call call = (Call) chargeable;
				if (call.getType() != Call.CALL_TYPE_SENT) {
					continue;
				}
				
				double callPrice = 0;
				
				if (call.getContact().getMsisdnType() == MsisdnType.ES_SPECIAL_ZER0) {
					callPrice = 0;
				} else {
					recipients.add(call.getContact().getMsisdn());
					secondsTotal += call.getDuration();

					boolean insideMinutesMonth = secondsTotal <= maxSecondsMonth;
					if (!insideMinutesMonth) {
						long duration = (secondsTotal > maxSecondsMonth) && (secondsTotal - call.getDuration() <= maxSecondsMonth)? secondsTotal - maxSecondsMonth : call.getDuration();  
						callPrice += initialPrice + (duration * pricePerSecond);
					} else if (recipients.size() > maxRecipients) {
						callPrice += initialPrice + (call.getDuration() * pricePerSecond);
					}
				}
				ret.addPlanCall(new PlanChargeable(call, callPrice, this.getCurrency()));
			} else if (chargeable.getChargeableType() == Chargeable.CHARGEABLE_TYPE_SMS) {
				Sms sms = (Sms) chargeable;
				if (sms.getType() == Sms.SMS_TYPE_RECEIVED) {
					continue;
				}
				smsTotal++;
				if (smsTotal <= maxSmsMonth) {
					ret.addPlanCall(new PlanChargeable(chargeable, 0, this.getCurrency()));
				} else {
					ret.addPlanCall(new PlanChargeable(chargeable, smsPrice, this.getCurrency()));
				}
			}
		}
		return ret;
	}
}