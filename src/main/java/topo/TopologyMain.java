package topo;


import spouts.RedisSpout;


import spouts.WordGenerator;
import utils.ConnectDatabase;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import bolts.WordCounter;
import bolts.WordNormalizer;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;

public class TopologyMain {
	
	public final static String REDIS_HOST = "localhost";
	public final static int REDIS_PORT = 6379;
	public final static int MYSQL_INTERVAL = 3000;
	public final static int STAT_DB = 3;

	public static boolean testing = false;
	
	public Jedis jedis;
	public String host; 
	public int port;

	public static void main(String[] args) throws InterruptedException, AlreadyAliveException, InvalidTopologyException {
        
        //Configuration
		Config conf = new Config();
		//conf.setMaxTaskParallelism(3);
		//conf.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 3);
		conf.setDebug(testing);
//		conf.setNumWorkers(10);
        conf.put("redis-host", REDIS_HOST);
        conf.put("redis-port", REDIS_PORT);
        conf.put("interval", MYSQL_INTERVAL);
        conf.put("stat-db", STAT_DB);
        //conf.put("webserver", WEBSERVER);
        //conf.put("download-time", DOWNLOAD_TIME);
        
		
        //Topology definition
		TopologyBuilder builder = new TopologyBuilder();
		
		//builder.setSpout("reader",new WordReader());
		
		//builder.setBolt("normalizer", new WordNormalizer())
		//	.shuffleGrouping("reader");
//		TailFileSpout Tailspout = new TailFileSpout("src/main/resources/test.log");
		RedisSpout rs = new RedisSpout();
		
		//builder.setSpout("generator",new WordGenerator(),1);
		builder.setSpout("generator",rs,1);
		
		builder.setBolt("normalizer", new WordNormalizer(),1)
			.shuffleGrouping("generator");
		
		builder.setBolt("counter", new WordCounter(),1)
			.fieldsGrouping("normalizer", new Fields("word"));
       
        //Topology run
//		LocalCluster cluster = new LocalCluster();
//		cluster.submitTopology("wordcount", conf, builder.createTopology());
//		Thread.sleep(600000);
//		cluster.shutdown();
		StormSubmitter.submitTopology("word_count", conf,builder.createTopology());
	
	}
}