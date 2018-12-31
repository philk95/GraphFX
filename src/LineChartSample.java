import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import lejos.hardware.BrickFinder;
import lejos.hardware.BrickInfo;

public class LineChartSample extends Application implements Runnable {
	private final static int PORT = 1235;
	private Socket connection;
	private int i = 1;
	private final XYChart.Series series = new XYChart.Series();

	@Override
	public void start(Stage stage) {
		stage.setTitle("Line Chart Sample");
		// defining the axes
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("Number of Month");
		// creating the chart
		final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);

		lineChart.setTitle("Stock Monitoring, 2010");
		// defining a series
		series.setName("My portfolio");
		// populating the series with data

		Scene scene = new Scene(lineChart, 800, 600);
		lineChart.getData().add(series);

		Thread t = new Thread(this);
		t.setDaemon(false);
		t.start();

		stage.setScene(scene);
		stage.show();

	}

	public void connectToFirstBrick() throws UnknownHostException, IOException {
		BrickInfo[] bricks = BrickFinder.discover();

		if (bricks.length == 0) {
			throw new IllegalArgumentException("No brick found!");
		}

		this.connection = new Socket(bricks[0].getIPAddress(), PORT);
		System.out.println("Connected: " + this.connection);
		this.connection.setTcpNoDelay(true);
	}

	public static void main(String[] args) {
		launch(args);

	}

	@Override
	public void run() {
		try {
			connectToFirstBrick();

			InputStream inputStream = connection.getInputStream();

			while (!connection.isClosed()) {
				int length = inputStream.available();

				if (length > 0) {
					byte[] read = new byte[length];
					inputStream.read(read);

					String command = new String(read);
					final double point = Double.parseDouble(command);

					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							series.getData().add(new XYChart.Data(i, point));
							i++;
						}
					});
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}