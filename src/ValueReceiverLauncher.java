import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.stage.Stage;

public class ValueReceiverLauncher extends Application {

	private ValueReceiver receiver;

	@Override
	public void start(Stage stage) {
		stage.setTitle("Remote Value Receiver");
		// defining the axes
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("Number of Month");
		// creating the chart
		final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);

		lineChart.setTitle("Reglungsdaten");
		lineChart.setCreateSymbols(false);
		// defining a series

		Scene scene = new Scene(lineChart, 800, 600);

		this.receiver = new ValueReceiver(1235, lineChart);

		stage.setScene(scene);
		stage.show();

		this.receiver.start();

	}

	@Override
	public void stop() {
		System.out.println("Stage is closing. So close the connection");
		receiver.close();
	}

	public static void main(String[] args) {
		launch(args);

	}

}