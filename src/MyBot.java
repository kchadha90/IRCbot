//Copyright 2014, Karan Chadha, All rights reserved.
//
//
//Commands
// ".listname" - Generates a list of renames in the form of old1 changed to new1
// ".users" - Generates structures of all users present in the channel
// ".names" - Prints a list of nick's present in the channel in the console
// ".add" - Adds another bot to the channel( with the same config for now)
// ".channel {user}" - Prints the list of channels joined by the user in the console
// ".quit {id}" - Quits the bot with id - {id}
// ".quit all" - Quits all the bots




import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pircbotx.Configuration;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.cap.TLSCapHandler;
import org.pircbotx.hooks.*;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.types.GenericMessageEvent;

@SuppressWarnings("rawtypes")
public class MyBot extends ListenerAdapter {

	static int no_of_bots = 0;
	static MultiBotManager<PircBotX> manager = new MultiBotManager<PircBotX>();
	Map<String, String> names = new HashMap<String, String>();
	static String server, channel, nick, realname, login;
	static String current_users = "Info not available yet";
	//static String users_in_channel = "Info not available yet";
	static int users;

	public void onGenericMessage(GenericMessageEvent event) throws Exception {
		ArrayList<String> links = new ArrayList<String>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String bot_nick = event.getBot().getNick().toString();
		String channel = event.getBot().getConfiguration()
				.getAutoJoinChannels().toString();

		String regex = "\\(?\\b(http|https|file|ftp://|www|➡[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(event.getMessage());
		String urllog = new String();
		while (m.find()) {
			links.add(m.group());
		}
		try {
			if(bot_nick != ""){ // bug
			// The Url file path is.
			File UrlText = new File("./logs/" + bot_nick + channel + "url.txt");
			FileWriter fw = new FileWriter(UrlText, true);
			urllog = event.getBot().getNick() + " " + dateFormat.format(date)
					+ " - " + event.getUser().getNick() + "!"
					+ event.getUser().getHostmask() + " "
					+ event.getBot().getConfiguration().getAutoJoinChannels()
					+ " :" + links.toString();
			if(links.toString() != "[ ]" || links.toString() != "[]"){ // bug 
			fw.write(urllog + "\n");
			fw.close();
			}
		}} catch (IOException e) {
			System.err.println("Problem writing to the Log file");
		}
		
		// Finding IRC server names inside the chat log

		String pattern1 = "irc.*.[A-Z|a-z]{2,3}";
		Pattern p1 = Pattern.compile(pattern1);
		Matcher m1 = p1.matcher(event.getMessage());
		while (m1.find()) {
			System.out.println("Found irc server name:" + m1.group());
		}
		
		String log = new String();
		log = event.getBot().getNick() + " " + dateFormat.format(date) + " - "
				+ event.getUser().getNick() + "!"
				+ event.getUser().getHostmask() + " "
				+ event.getBot().getConfiguration().getAutoJoinChannels()
				+ " :" + event.getMessage();
		generating_logs(log, bot_nick, channel);
	}

	public void generating_logs(String log, String bot_nick, String channel) {
		try {
			// The file path is.
			if(bot_nick!=""){
			File LogText = new File("./logs/" + bot_nick + channel + "log.txt");
			FileWriter fw = new FileWriter(LogText, true);
			fw.write(log + "\n");
			fw.close();
		}} catch (IOException e) {
			System.err.println("Problem writing to the Log file");
		}

	}

	@SuppressWarnings("unchecked")
	public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
		if (event.getMessage().equals(".listname")) {
			// Generate a list of renames in the form of old1 changed to new1,
			// old2 changed to new2, etc
			String users = "";
			for (Map.Entry<String, String> curUser : names.entrySet()) {
				// Add original nick
				users += curUser.getKey();
				// Add middle text
				users += " changed to ";
				// Add new nick
				users += curUser.getValue();
				// Add separator
				users += ", ";
			}
			// Remove last separator

			try {
				users = users.substring(0, users.length() - 2);

				// Send to user in a PM
				event.respond("Renamed users:" + users); // LOG
			} catch (Exception e) {
				event.respond("No data found");
			}
		}

		// Execute carefully on a crowded channel. Let there be a first join
		// before
		// it populates the information of users
		// the list updates automatically on every join
		// parse whatever information you need here ( We can make the DNA of
		// user from this info)

		if (event.getMessage().equals(".users")) {
			/*
			 * int startIndex = 0; int endIndex = 0; String print_users =
			 * "Users ="; current_users.toCharArray(); while (startIndex != -1)
			 * { startIndex = current_users.indexOf("nick", startIndex); if
			 * (startIndex != -1) { endIndex = current_users.indexOf(",",
			 * startIndex); print_users = print_users +
			 * current_users.substring(startIndex + 5, endIndex) + " ,";
			 * startIndex += (endIndex - startIndex); } }
			 */
			event.respond(current_users);

		}
		// Returns names of users in specified channel
		if (event.getMessage().toLowerCase().startsWith(".names ")) {
			String chann_name = event.getMessage()
					.substring(event.getMessage().indexOf(" ")).trim();
			// for tracking
			// whoisRef.put(user.toLowerCase(), channel);
			event.getBot().sendRaw().rawLineNow("NAMES " + chann_name);
		}
		
		if(event.getMessage().toLowerCase().startsWith(".add")){
			@SuppressWarnings("unchecked")
			Configuration configuration = new Configuration.Builder()
			.setName("test")
			.setRealName("sample")
			.setLogin("tess")
			.setVersion("v1.3")
			.setFinger("ola")
			// login part of hostmask, eg name:login@host
			.setAutoNickChange(true)
			// Automatically change nick when the current one is in use
			.setCapEnabled(true)
			.setAutoReconnect(true)
			// Enable CAP features
			.addCapHandler(
					new TLSCapHandler(new UtilSSLSocketFactory()
							.trustAllCertificates(), true))
			.addListener(new MyBot())
			// This class is a
			// listener, so add it to the bots known listeners
			.setServerHostname(server).addAutoJoinChannel(channel)
			.buildConfiguration();

	// For multiple bots repeat the procedure
	try {
		manager.addBot(configuration);
	} catch (Exception ex) {
		ex.printStackTrace();
		}
		}

		/*
		 * if (event.getMessage().equals(".userchannels")) { // EXPENSIVE Call
		 * 
		 * int startIndex = 0; int endIndex = 0; String print_whois = "";
		 * current_users.toCharArray(); while (startIndex != -1) { startIndex =
		 * current_users.indexOf("nick", startIndex); if (startIndex != -1) {
		 * endIndex = current_users.indexOf(",", startIndex); String u =
		 * current_users .substring(startIndex + 5, endIndex); print_whois =
		 * print_whois + event.getBot().sendRaw().rawLineNow("WHOIS " + u);
		 * startIndex += (endIndex - startIndex); } }
		 * 
		 * }
		 */

		// performs whois on the user
		if (event.getMessage().toLowerCase().startsWith(".channel ")) {
			String user = event.getMessage()
					.substring(event.getMessage().indexOf(" ")).trim();
			event.getBot().sendRaw().rawLineNow("WHOIS " + user);
		}
		
		/*if (event.getMessage().toLowerCase().startsWith(".all_channels")) {
			event.respond(users_in_channel);
		}*/

		// Quits a particular bot with specified ID
		if (event.getMessage().toLowerCase().startsWith(".quit ")) {
			String id = event.getMessage()
					.substring(event.getMessage().indexOf(" ")).trim();
			int bot_id = Integer.parseInt(id);
			manager.getBotById(bot_id).sendRaw().rawLineNow("QUIT");
		}

		// Quits all the bots
		if (event.getMessage().toLowerCase().startsWith(".quit all")) {
			manager.stop();
		}

	}

	public void onJoin(JoinEvent event) throws Exception {
		current_users = event.getChannel().getUsers().toString(); // Set of
																	// users
		users = event.getChannel().getUsers().size(); // Number of users
		System.out.println(users + " present currently");
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String log = new String();
		log = event.getBot().getNick() + " " + dateFormat.format(date) + " - "
				+ event.getUser().getNick() + "!"
				+ event.getUser().getHostmask() + " "
				+ event.getBot().getConfiguration().getAutoJoinChannels()
				+ ": ^JOINED";

		String bot_nick = event.getBot().getNick().toString();
		String channel = event.getBot().getConfiguration()
				.getAutoJoinChannels().toString();
		generating_logs(log, bot_nick, channel);
	}

	public void onQuit(QuitEvent event) throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String log = new String();
		log = event.getBot().getNick() + " " + dateFormat.format(date) + " - "
				+ event.getUser().getNick() + "!"
				+ event.getUser().getHostmask() + " "
				+ event.getBot().getConfiguration().getAutoJoinChannels()
				+ ": ^LEFT - " + event.getReason();

		String bot_nick = event.getBot().getNick().toString();
		String channel = event.getBot().getConfiguration()
				.getAutoJoinChannels().toString();
		generating_logs(log, bot_nick, channel);
	}

	public void onPart(PartEvent event) throws Exception {
		// current_users = event.getChannel().getUsers().toString();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String log = new String();
		log = event.getBot().getNick() + " " + dateFormat.format(date) + " - "
				+ event.getUser().getNick() + "!"
				+ event.getUser().getHostmask() + " "
				+ event.getBot().getConfiguration().getAutoJoinChannels()
				+ ": ^LEFT - " + event.getReason();

		String bot_nick = event.getBot().getNick().toString();
		String channel = event.getBot().getConfiguration()
				.getAutoJoinChannels().toString();
		generating_logs(log, bot_nick, channel);
	}

	public void onNickChange(NickChangeEvent event) throws Exception {
		// Store the result
		names.put(event.getOldNick(), event.getNewNick());
	}

	public void onKick(KickEvent event) throws Exception {
		if (event.getUser().getNick().equals(event.getBot().getNick())) {
			event.getBot().sendIRC().joinChannel(event.getChannel().toString());
		}
	}

	public void onUserList(UserListEvent event) throws Exception {
		//users_in_channel = event.getUsers().toString();
		System.out.println(event.getUsers().toString()); // List of users in
															// channel
	}

	public void onWhois(WhoisEvent event) throws Exception {
		System.out.println(event.getChannels().toString()); // List of channels
															// joined per user
	}

	@SuppressWarnings({ "unchecked" })
	public static void main(String[] args) throws Exception {
		int i = 0, tor_confirm;
		// Data structure to hold config's for different bots
		Map<String, Configuration> config = new HashMap<String, Configuration>();
		Scanner in = new Scanner(System.in);
		
		Properties p = new Properties(); // Reading the Bot config from the file
		p.load(new FileInputStream(new File("./config.ini")));
		
		System.out.println("Do you want to use Tor network settings? Enter \"1\" for yes, \"0\" for no");
		tor_confirm = in.nextInt();
		
		if(tor_confirm == 1){
		System.setProperty("socksProxyHost","127.0.0.1");
		System.setProperty("socksProxyPort","9150");
		System.out.println("Tor settings established..");
		}else{
		System.out.println("Using normal connection..");
		}
		
		System.out.println("No of bots?:");
		no_of_bots = in.nextInt();

		for (i = 0; i < no_of_bots; i++) {
			server = p.getProperty("Server" + i, "localhost");
			channel = p.getProperty("Channel" + i, "#test");
			nick = p.getProperty("Nick" + i, "hdk");
			realname = p.getProperty("RealName" + i, "raunak");
			login = p.getProperty("Login" + i, "r_hdb");
			Configuration configuration = new Configuration.Builder()
					.setName(nick)
					.setRealName(realname)
					.setLogin(login)
					.setVersion("v1.3")
					.setFinger("ola")
					// login part of hostmask, eg name:login@host
					.setAutoNickChange(true)
					// Automatically change nick when the current one is in use
					.setCapEnabled(true)
					.setAutoReconnect(true)
					// Enable CAP features
					.addCapHandler(
							new TLSCapHandler(new UtilSSLSocketFactory()
									.trustAllCertificates(), true))
					.addListener(new MyBot())
					// This class is a
					// listener, so add it to the bots known listeners
					.setServerHostname(server).addAutoJoinChannel(channel)
					.buildConfiguration();

			config.put("config" + i, configuration);
			// For multiple bots repeat the procedure
			try {
				manager.addBot(config.get("config" + i));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		manager.start();
		in.close();
	}

}