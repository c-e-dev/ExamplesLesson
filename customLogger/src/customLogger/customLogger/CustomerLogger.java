package customLogger.customLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.Integer;
import java.lang.Boolean;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class CustomerLogger {
    static public class lg{
        static public Logger log;
    }
    private String loggingLevel;
    private String loggingSize_log;
    private String loggingDirectory;
    private String loggingPattern;
    private int loggingRotation;

    private String parseConfigFile(String section, String param) throws Exception{
        String value = ""; //Значение для вывода
        final String contentFile = new String(Files.readAllBytes(Paths.get("//media/adan/D/Develop/Git/GitHub/ExamplesLesson/customLogger/src/customLogger.conf")), StandardCharsets.UTF_8); //файл с конфигурацией

        String regex = "\\["+section+"\\].*?[^#]"+param+"\\=(.*?)(\\n|$)"; //формируем правило для регулярки

        final Pattern patte = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE);
        final Matcher match = patte.matcher(contentFile);

        match.find(); //инициализируем поиск
        value = match.group(1);

        return value;
    }

    public void init() throws Exception{

        loggingLevel = this.parseConfigFile("logging", "level");
        loggingDirectory = this.parseConfigFile("logging", "directory");
        loggingSize_log = this.parseConfigFile("logging", "size_log");
        loggingPattern = this.parseConfigFile("logging", "pattern");
        loggingRotation = Integer.parseInt(this.parseConfigFile("logging", "rotation"));

        Level lvl = Level.INFO; //задаем уровень логирования, подтягиваем из файла конфигурации
        switch(loggingLevel) {
            case "info":
                lvl = Level.INFO;
                break;
            case "standard":
                lvl = Level.INFO;
                break;
            case "trace":
                lvl = Level.TRACE;
                break;
            case "debug":
                lvl = Level.DEBUG;
                break;
            case "all":
                lvl = Level.ALL;
                break;
            case "none":
                lvl = Level.OFF;
                break;
            default:
                lvl = Level.ALL;
                break;
        }

        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.ERROR); //уровень логирования самого Logger при инициализации
        builder.setConfigurationName("RollingBuilder"); //название конфигуратора логирования
        builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL).addAttribute("level", lvl)); //Пороговые фильтры

        AppenderComponentBuilder file = builder.newAppender("Stdout", "CONSOLE").addAttribute("target",
                ConsoleAppender.Target.SYSTEM_OUT);
        file.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
        builder.add(file);

        LayoutComponentBuilder standard = builder.newLayout("PatternLayout").addAttribute("pattern", "%d %-5level: %msg%n%throwable");
        file.add(standard); //формат логирования (Дата, Уровень логирования, Передаваемое сообщение в логер)

        ComponentBuilder triggeringPolicy = builder.newComponent("Policies");
        triggeringPolicy.addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", "0 0 0 * * ?"));//периодичность создания нового лога
        triggeringPolicy.addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", loggingSize_log));//размер лога при котором будет создан новый лог

        file = builder.newAppender("rolling", "RollingFile"); //инициализация блока для настройки записи в файл
        file.addAttribute("fileName", loggingDirectory); //директория+имя файла, желательное полное имя
        file.addAttribute("filePattern", loggingRotation); //директория+имя файла при ротации
        file.add(standard);
        file.addComponent(triggeringPolicy);
        builder.add(file);

        //Добавление логгеров, по которым будет происходить логирование.
        builder.add(builder.newLogger("customLogger", lvl).add(builder.newAppenderRef("rolling")).addAttribute("additivity", false));
        //Настройка рут логера, в случае если сообщения не будут определены через newLogger
        builder.add(builder.newRootLogger(lvl).add(builder.newAppenderRef("rolling")));

        Configurator.initialize(builder.build());//Инициализируем весь соборанный конфиг

        //Получаем имя логгер, позже созданного (имя getLogger должно быть таким же как в newLogger, иначе будет происходить по newRootLogger, со своим уровнем логирования)
        lg.log = LogManager.getLogger("customLogger");

        lg.log.info("Запускается Logger...");

    }
}
