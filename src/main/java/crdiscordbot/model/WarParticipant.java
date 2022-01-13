/*
 * Clash Royale API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: v1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package crdiscordbot.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * WarParticipant
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2022-01-09T15:49:55.865Z")
public class WarParticipant {
  @SerializedName("tag")
  private String tag = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("cardsEarned")
  private Integer cardsEarned = null;

  @SerializedName("battlesPlayed")
  private Integer battlesPlayed = null;

  @SerializedName("wins")
  private Integer wins = null;

  public WarParticipant tag(String tag) {
    this.tag = tag;
    return this;
  }

   /**
   * Get tag
   * @return tag
  **/
  @ApiModelProperty(value = "")
  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public WarParticipant name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public WarParticipant cardsEarned(Integer cardsEarned) {
    this.cardsEarned = cardsEarned;
    return this;
  }

   /**
   * Get cardsEarned
   * @return cardsEarned
  **/
  @ApiModelProperty(value = "")
  public Integer getCardsEarned() {
    return cardsEarned;
  }

  public void setCardsEarned(Integer cardsEarned) {
    this.cardsEarned = cardsEarned;
  }

  public WarParticipant battlesPlayed(Integer battlesPlayed) {
    this.battlesPlayed = battlesPlayed;
    return this;
  }

   /**
   * Get battlesPlayed
   * @return battlesPlayed
  **/
  @ApiModelProperty(value = "")
  public Integer getBattlesPlayed() {
    return battlesPlayed;
  }

  public void setBattlesPlayed(Integer battlesPlayed) {
    this.battlesPlayed = battlesPlayed;
  }

  public WarParticipant wins(Integer wins) {
    this.wins = wins;
    return this;
  }

   /**
   * Get wins
   * @return wins
  **/
  @ApiModelProperty(value = "")
  public Integer getWins() {
    return wins;
  }

  public void setWins(Integer wins) {
    this.wins = wins;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WarParticipant warParticipant = (WarParticipant) o;
    return Objects.equals(this.tag, warParticipant.tag) &&
        Objects.equals(this.name, warParticipant.name) &&
        Objects.equals(this.cardsEarned, warParticipant.cardsEarned) &&
        Objects.equals(this.battlesPlayed, warParticipant.battlesPlayed) &&
        Objects.equals(this.wins, warParticipant.wins);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tag, name, cardsEarned, battlesPlayed, wins);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WarParticipant {\n");
    
    sb.append("    tag: ").append(toIndentedString(tag)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    cardsEarned: ").append(toIndentedString(cardsEarned)).append("\n");
    sb.append("    battlesPlayed: ").append(toIndentedString(battlesPlayed)).append("\n");
    sb.append("    wins: ").append(toIndentedString(wins)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
