package org.socialsignin.spring.data.dynamodb.marshaller;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;

public class Date2EpocheDynamoDBMarshaller extends DateDynamoDBMarshaller {

	private static final class EpcoheDateFormat extends DateFormat {
		private static final long serialVersionUID = 2969564523817434535L;

		@Override
		public StringBuffer format(Date date, StringBuffer toAppendTo,
				FieldPosition fieldPosition) {
			long epoche = date.getTime();
			toAppendTo.append(epoche);
			return toAppendTo;
		}

		@Override
		public Date parse(String source, ParsePosition pos) {
			long epoche = Long.parseLong(source);
			pos.setIndex(source.length());
			return new Date(epoche);
		}
		
	};

	@Override
	public DateFormat getDateFormat() {
		return new EpcoheDateFormat();
	}

}
