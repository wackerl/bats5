import java.util.Comparator;


class SortLabel implements Comparator<Run>
{
	public int compare(Run a, Run b )
	{
		int result = 0;
		if ((result = a.sample.label.compareTo(b.sample.label)) == 0)
		{
			//If same last name, sort on second element
			try {
				if ((result = a.sample.posit.compareTo(b.sample.posit)) == 0) {
				    result = a.recno.compareTo(b.recno);
				};
			} catch (NullPointerException e) 
			{;}
		}			 
		return result;
	}
}
