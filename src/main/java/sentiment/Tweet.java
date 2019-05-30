package sentiment;

public class Tweet {
//int numero;	
String lang, sentiment, follow_request_sent, description, profile_text_color, location, id_str, 
username, langAu, name, urlAu, created_at, time_zone, text, domain, content_type, f_content_type,
string_date, type, idTw;
boolean saved, verified, protecte, default_profile_image, geo_enabled, default_profile ;
long virality, score, reach, weight, id, followers_count,statuses_count, friends_count, favourites_count,  listed_count;

public Tweet() {}

public Tweet(String lang,String sentiment,String follow_request_sent,String description,String profile_text_color,String location,String id_str, 
		String username,String langAu,String name,String urlAu,String created_at,String time_zone,String text,String domain,String content_type,String f_content_type,
		String string_date,String type,String idTw,
		boolean saved,boolean verified,boolean protecte,boolean default_profile_image,boolean geo_enabled,boolean default_profile,
		long virality,long score,long reach,long weight,long id,long followers_count,long statuses_count,long friends_count, 
		long favourites_count,long listed_count) {
	
	//this.numero = numero;
	this.lang = lang;
	this.sentiment = sentiment;
	this.follow_request_sent = follow_request_sent;
	this.description = description;
	this.profile_text_color = profile_text_color;
	this.location = location;
	this.id_str = id_str;
	this.username = username;
	this.langAu = langAu;
	this.name = name;
	this.urlAu = urlAu;
	this.created_at = created_at;
	this.time_zone = time_zone;
	this.text = text;
	this.domain = domain;
	this.content_type = content_type;
	this.f_content_type = f_content_type;
	this.string_date = string_date;
	this.type = type;
	this.idTw = idTw;
	this.saved = saved;
	this.verified = verified;
	this.protecte = protecte; 
	this.default_profile_image = default_profile_image;  
	this.geo_enabled = geo_enabled;
	this.default_profile = geo_enabled;
	this.virality = virality;
	this.score = score;
	this.reach = reach; 
	this.weight = weight; 
	this.id = id;
	this.followers_count = followers_count;
	this.favourites_count = favourites_count;
	this.statuses_count =  statuses_count;
	this.friends_count = friends_count;  
	this.listed_count = listed_count;

}



public String getLang() {
	return lang;
}
public void setLang(String lang) {
	this.lang = lang;
}
public String getSentiment() {
	return sentiment;
}
public void setSentiment(String sentiment) {
	this.sentiment = sentiment;
}
public String getFollow_request_sent() {
	return follow_request_sent;
}
public void setFollow_request_sent(String follow_request_sent) {
	this.follow_request_sent = follow_request_sent;
}
public String getDescription() {
	return description;
}
public void setDescription(String description) {
	this.description = description;
}
public String getProfile_text_color() {
	return profile_text_color;
}
public void setProfile_text_color(String profile_text_color) {
	this.profile_text_color = profile_text_color;
}
public String getLocation() {
	return location;
}
public void setLocation(String location) {
	this.location = location;
}
public String getId_str() {
	return id_str;
}
public void setId_str(String id_str) {
	this.id_str = id_str;
}
public String getUsername() {
	return username;
}
public void setUsername(String username) {
	this.username = username;
}
public String getLangAu() {
	return langAu;
}
public void setLangAu(String langAu) {
	this.langAu = langAu;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getUrlAu() {
	return urlAu;
}
public void setUrlAu(String urlAu) {
	this.urlAu = urlAu;
}
public String getCreated_at() {
	return created_at;
}
public void setCreated_at(String created_at) {
	this.created_at = created_at;
}
public String getTime_zone() {
	return time_zone;
}
public void setTime_zone(String time_zone) {
	this.time_zone = time_zone;
}
public String getText() {
	return text;
}
public void setText(String text) {
	this.text = text;
}
public String getDomain() {
	return domain;
}
public void setDomain(String domain) {
	this.domain = domain;
}
public String getContent_type() {
	return content_type;
}
public void setContent_type(String content_type) {
	this.content_type = content_type;
}
public String getF_content_type() {
	return f_content_type;
}
public void setF_content_type(String f_content_type) {
	this.f_content_type = f_content_type;
}
public String getString_date() {
	return string_date;
}
public void setString_date(String string_date) {
	this.string_date = string_date;
}
public String getType() {
	return type;
}
public void setType(String type) {
	this.type = type;
}
public String getIdTw() {
	return idTw;
}
public void setIdTw(String idTw) {
	this.idTw = idTw;
}
public boolean isSaved() {
	return saved;
}
public void setSaved(boolean saved) {
	this.saved = saved;
}
public boolean isVerified() {
	return verified;
}
public void setVerified(boolean verified) {
	this.verified = verified;
}
public boolean isProtecte() {
	return protecte;
}
public void setProtecte(boolean protecte) {
	this.protecte = protecte;
}
public boolean isDefault_profile_image() {
	return default_profile_image;
}
public void setDefault_profile_image(boolean default_profile_image) {
	this.default_profile_image = default_profile_image;
}
public boolean isGeo_enabled() {
	return geo_enabled;
}
public void setGeo_enabled(boolean geo_enabled) {
	this.geo_enabled = geo_enabled;
}
public boolean isDefault_profile() {
	return default_profile;
}
public void setDefault_profile(boolean default_profile) {
	this.default_profile = default_profile;
}
public long getVirality() {
	return virality;
}
public void setVirality(long virality) {
	this.virality = virality;
}
public long getScore() {
	return score;
}
public void setScore(long score) {
	this.score = score;
}
public long getReach() {
	return reach;
}
public void setReach(long reach) {
	this.reach = reach;
}
public long getWeight() {
	return weight;
}
public void setWeight(long weight) {
	this.weight = weight;
}
public long getId() {
	return id;
}
public void setId(long id) {
	this.id = id;
}
public long getFollowers_count() {
	return followers_count;
}
public void setFollowers_count(long followers_count) {
	this.followers_count = followers_count;
}
public long getStatuses_count() {
	return statuses_count;
}
public void setStatuses_count(long statuses_count) {
	this.statuses_count = statuses_count;
}
public long getFriends_count() {
	return friends_count;
}
public void setFriends_count(long friends_count) {
	this.friends_count = friends_count;
}
public long getFavourites_count() {
	return favourites_count;
}
public void setFavourites_count(long favourites_count) {
	this.favourites_count = favourites_count;
}
public long getListed_count() {
	return listed_count;
}
public void setListed_count(long listed_count) {
	this.listed_count = listed_count;
}


}
