import java.util.Comparator;

import org.jfree.util.Log;


class SortRun implements Comparator<Run>
{
	public int compare(Run a, Run b )
	{
		int result = 0;
		try {
			result = a.run.compareTo(b.run);
			Log.debug(a.recno+"-"+b.recno);
		} catch (NullPointerException e) {
			Log.debug(e.getMessage());
		}
		return result;
	}

}
