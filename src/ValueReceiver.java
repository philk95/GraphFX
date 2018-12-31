import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import lejos.hardware.BrickFinder;
import lejos.hardware.BrickInfo;

public class ValueReceiver extends Thread {

	private Socket connection;
	private int port;
	private final LineChart<Number, Number> chart;
	private Pattern REGEX = Pattern.compile("\\[(.*):(.*)\\]");

	public static void main(String[] args) {
		Pattern REGEX = Pattern.compile("\\[.*:(.*)\\]");
		Matcher matcher = REGEX.matcher("[key:value]");
		System.out.println(matcher.matches());
		System.out.println(matcher.group(1));
	}

	private HashMap<String, XYChart.Series<Number, Number>> allSeries = new HashMap<>();

	public ValueReceiver(int port, LineChart<Number, Number> chart) {
		this.port = port;
		this.chart = chart;
		setDaemon(false);
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

					String value = new String(read);
					processReceivedData(value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void processReceivedData(String message) {
		Matcher matcher = REGEX.matcher(message);
		if (matcher.matches()) {
			try {
				String key = matcher.group(1);
				double point = Double.parseDouble(matcher.group(2));

				addDatapoint(key, point);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("No match for: " + message);
		}

	}

	private void addDatapoint(final String key, final double point) {
		XYChart.Series<Number, Number> series = allSeries.get(key);
		if (series == null) {
			series = new XYChart.Series<>();
			series.setName(key);
			
			final XYChart.Series<Number, Number> immutalbeSeries = series;
			FutureTask<Void> addChart = new FutureTask<>(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					chart.getData().add(immutalbeSeries);
					
					return null;
				}
			});

			Platform.runLater(addChart);
			try {
				addChart.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

			allSeries.put(key, series);
		}

		final XYChart.Series<Number, Number> immutableSeries = series;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				int nextIndex = immutableSeries.getData().size();
				immutableSeries.getData().add(new XYChart.Data<Number, Number>(nextIndex, point));
			}
		});
	}

	public void connectToFirstBrick() throws UnknownHostException, IOException {
		BrickInfo[] bricks = BrickFinder.discover();

		if (bricks.length == 0) {
			throw new IllegalArgumentException("No brick found!");
		}

		this.connection = new Socket(bricks[0].getIPAddress(), port);
		System.out.println("Connected: " + this.connection);
		this.connection.setTcpNoDelay(true);
	}

	public void close() {
		if (this.connection != null) {
			try {
				this.connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
