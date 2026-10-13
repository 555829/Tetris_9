package game;

import game.profile.ProfileImageCodec;

import java.io.IOException;

//======================================================================================================================
// 로그인된 사용자 정보 저장하는 곳입니다
//======================================================================================================================
final class UserProfileState {
    private final String nickname;
    private final int money;
    private final int playCount;
    private final String createdAt;
    private final String equippedSkin;
    private final boolean hasWhiteCard;
    private final boolean hasSilverCard;
    private final boolean hasGoldCard;
    private final boolean hasPlatinumCard;
    private final byte[] profileImage;

    private UserProfileState(String nickname, int money, int playCount, String createdAt,
                             String equippedSkin, boolean hasWhiteCard, boolean hasSilverCard,
                             boolean hasGoldCard, boolean hasPlatinumCard, byte[] profileImage) {
        this.nickname = nickname;
        this.money = money;
        this.playCount = playCount;
        this.createdAt = createdAt;
        this.equippedSkin = equippedSkin;
        this.hasWhiteCard = hasWhiteCard;
        this.hasSilverCard = hasSilverCard;
        this.hasGoldCard = hasGoldCard;
        this.hasPlatinumCard = hasPlatinumCard;
        this.profileImage = profileImage == null ? null : profileImage.clone();
    }

    static UserProfileState empty() {
        return new UserProfileState("", 0, 0, "", "WHITE", true,
                false, false, false, null);
    }

    static UserProfileState fromUserInfoPacket(String[] tokens) {
        if (tokens == null || tokens.length < 10 || !"RES_USER_INFO".equals(tokens[0])
                || "FAIL".equals(tokens[1])) return null;

        byte[] image = null;
        if (tokens.length >= 11 && !tokens[10].trim().isEmpty()) {
            try {
                image = ProfileImageCodec.decodeFromServer(tokens[10]);
            } catch (IllegalArgumentException | IOException ignored) {
                // 손상된 이미지 데이터는 프로필 나머지 정보의 갱신을 막지 않습니다.
            }
        }

        return new UserProfileState(tokens[1], Integer.parseInt(tokens[2]),
                Integer.parseInt(tokens[3]), tokens[4], tokens[5],
                "1".equals(tokens[6]), "1".equals(tokens[7]),
                "1".equals(tokens[8]), "1".equals(tokens[9]), image);
    }

    UserProfileState withNickname(String value) {
        return new UserProfileState(value, money, playCount, createdAt, equippedSkin,
                hasWhiteCard, hasSilverCard, hasGoldCard, hasPlatinumCard, profileImage);
    }

    UserProfileState withProfileImage(byte[] value) {
        return new UserProfileState(nickname, money, playCount, createdAt, equippedSkin,
                hasWhiteCard, hasSilverCard, hasGoldCard, hasPlatinumCard, value);
    }

    String getNickname() { return nickname; }
    int getMoney() { return money; }
    int getPlayCount() { return playCount; }
    String getCreatedAt() { return createdAt; }
    String getEquippedSkin() { return equippedSkin; }
    boolean hasWhiteCard() { return hasWhiteCard; }
    boolean hasSilverCard() { return hasSilverCard; }
    boolean hasGoldCard() { return hasGoldCard; }
    boolean hasPlatinumCard() { return hasPlatinumCard; }
    byte[] getProfileImage() { return profileImage == null ? null : profileImage.clone(); }
}
