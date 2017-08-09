package nz.ac.auckland.concert.domain;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Class to represent a Concert.
 *
 */
public class Concert implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long _id;
	private String _title;
	private DateTime _date;

	public Concert() {}
	
	public Concert(Long id, String title, DateTime date) {
		_id = id;
		_title = title;
		_date = date;
	}
	
	public Concert(String title, DateTime date) {
		this(null, title, date);
	}
	
	public void setTitle(String title) {
		_title = title;
	}
	
	public void setDate(DateTime date) {
		_date = date;
	}
	
	public Long getId() {
		return _id;
	}
	
	public String getTitle() {
		return _title;
	}
	
	public DateTime getDate() {
		return _date;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Concert: id "); 
		buffer.append(_id);
		buffer.append(", title ");
		buffer.append(_title);
		buffer.append(", date ");
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
		String date = formatter.print(_date);
		
		buffer.append(date);
		
		return buffer.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Concert))
            return false;
        if (other == this)
            return true;

        Concert rhs = (Concert) other;
        return new EqualsBuilder().
        	append(_id, rhs.getId()).
            append(_title, rhs.getTitle()).
           
            isEquals();
	}
	
	@Override 
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
				append(getClass().getName()).
	            append(_id).
	            append(_title).
	            toHashCode();
	}
}
