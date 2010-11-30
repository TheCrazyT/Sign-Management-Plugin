import java.util.Map;
import java.util.TimerTask;


public class UpdateSigns extends TimerTask {

	@Override
	public void run() {
		SignManagementPluginListener.SaveSignsTexts();
	}

}
