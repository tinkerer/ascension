import java.util.*;

import com.heroicrobot.dropbit.registry.*;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;

import heronarts.lx.*;
import heronarts.lx.model.*;
import heronarts.lx.pattern.*;
import heronarts.lx.transition.AddTransition;

import processing.data.*;

class Engine {

  LXPattern[] patterns(LX lx) {
    return new LXPattern[] {
      new HeartRadiusTestPattern(lx),
      new SolidColorExamplePattern(lx),
      new StaticPartsExamplePattern(lx),
      new BasicAnimationPattern(lx),
      new HeartPhiTestPattern(lx),
      new TrunkPhiTestPattern(lx),
      new PlantHeartWavePattern(lx),
      new TrunkLengthRainbowPattern(lx),
      new PulsePattern(lx),
      new HeartShellTestPattern(lx),
      new ModelTestPattern(lx),
      new AmirRandomPlay(lx),
      new AmirRegisterPlay(lx),
      new AmirSphere(lx),
      new SoundHistogram(lx),
    };
  }

  Model model;
  LX lx;
  DeviceRegistry ppRegistry;

  void start() {
    // prepareExitHandler();

    model = loadModel();
    lx = createLX(model);

    // Show render loop time
    // lx.engine.addLoopTask(new LXLoopTask() {
    //   int counter = 0;
    //   public void loop(double deltaMs) {
    //     LXPattern pattern = lx.engine.getDefaultChannel().getActivePattern();
    //     float runtime = pattern.timer.runNanos / 1000000.0f;
    //     counter = (counter+1) % 10;
    //     if (counter != 0) return;
    //     System.out.println(runtime);
    //   }
    // });

    lx.setPatterns(patterns(lx));
    lx.engine.getDefaultChannel().setFaderTransition(new AddTransition(lx));

    setupOutput();

    lx.engine.start();
  }

  LX createLX(LXModel model) {
    return new JavaLX(model);
  }

  Model loadModel() {
    Table ledTable = Utils.loadTable("led-locations.csv", "header,csv");
    Table ppTable = Utils.loadTable("pp-strip-map.csv", "header,csv");
    if (ledTable == null) {
      System.out.println("Error: could not load LED position data");
      System.exit(1);
    }
    if (ppTable == null) {
      System.out.println("Error: could not load pixel pusher strip data");
      System.exit(1);
    }
    return new Model(ledTable, ppTable);
  }

  void setupPPRegistry() {
    ppRegistry = new DeviceRegistry();
    ppRegistry.setLogging(false);
    ppRegistry.setExtraDelay(0);
    ppRegistry.setAutoThrottle(true);
    ppRegistry.setAntiLog(true);
  }

  void setupOutput() {
    setupPPRegistry();
    lx.addOutput(new PixelPusherOutput(lx, ppRegistry));
  }

  private void prepareExitHandler () {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        System.out.println("Shutdown hook running");

        List<Strip> strips = ppRegistry.getStrips();
        for (Strip strip : strips) {
          for (int i=0; i<strip.getLength(); i++)
            strip.setPixel(0, i);
        }
        for (int i=0; i<100000; i++)
          Thread.yield();
      }
    }));
  }

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Use run.sh");
      System.exit(1);
    }
    String rootDirectory = args[0];

    Utils.sketchPath = rootDirectory;
    
    Engine engine = new Engine();
    engine.start();
  }

}

class JavaLX extends LX {
  JavaLX(LXModel model) {
    super(model);
  }
}