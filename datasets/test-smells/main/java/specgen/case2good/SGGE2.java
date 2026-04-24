package specgen.case2good;

;

enum MembershipLevel {
  BRONZE,
  SILVER,
  GOLD,
  PLATINUM
}

interface MembershipInterface {
  String getMemberId();
  MembershipLevel getLevel();
  int getPoints();
  void addPoints(int pts);
  boolean redeemPoints(int pts);
  String getBenefitsDescription();
}

interface BronzeMembershipInterfaceInterface extends MembershipInterface {
  void updateLevel();
}

class BronzeMembershipInterface implements MembershipInterface {
  protected final String memberId;
  protected int points;

  public BronzeMembershipInterface(String memberId) {
    this.memberId = memberId;
    this.points = 0;
  }

  public String getMemberId() {
    return this.memberId;
  }

  public MembershipLevel getLevel() {
    return MembershipLevel.BRONZE;
  }

  public int getPoints() {
    return this.points;
  }

  public void addPoints(int pts) {
    if (pts < 0) throw new IllegalArgumentException("pts must be non-negative");
    points += pts;
    updateLevel();
  }

  public boolean redeemPoints(int pts) {
    if (pts < 0) {
      throw new IllegalArgumentException("pts must be non-negative");
    }
    if (pts > points) {
      return false;
    }
    points -= pts;
    updateLevel();
    return true;
  }

  public void updateLevel() {
    if (points >= 1_000) {
      System.out.println("🎉 You’ve earned 1,000+ points—time to upgrade to SILVER!");
    }
  }

  public String getBenefitsDescription() {
    return "Bronze members earn 1 point per dollar.\n"
      + "Reach 1,000 points to upgrade to Silver and unlock bonus perks!";
  }
}

class CustomerDriver {
  private double brakingRisk;
  private double speedingRisk;
  private double distractionRisk;

  private int rewardPoints;
  private MembershipLevel membershipLevel;

  public CustomerDriver(double brakingRisk,
                        double speedingRisk,
                        double distractionRisk) {
    setBrakingRisk(brakingRisk);
    setSpeedingRisk(speedingRisk);
    setDistractionRisk(distractionRisk);
    this.rewardPoints = 0;
    this.membershipLevel = MembershipLevel.BRONZE;
  }

  public void setBrakingRisk(double v)     { validateFactor(v, "brakingRisk");     this.brakingRisk     = v; }
  public void setSpeedingRisk(double v)    { validateFactor(v, "speedingRisk");    this.speedingRisk    = v; }
  public void setDistractionRisk(double v) { validateFactor(v, "distractionRisk"); this.distractionRisk = v; }

  public double getBrakingRisk()     { return brakingRisk; }
  public double getSpeedingRisk()    { return speedingRisk; }
  public double getDistractionRisk() { return distractionRisk; }

  private void validateFactor(double value, String name) {
    if (value < 0.0 || value > 1.0) {
      throw new IllegalArgumentException(name + " must be between 0.0 and 1.0");
    }
  }

  public double calculateOverallRisk() {
    return (brakingRisk + speedingRisk + distractionRisk) / 3.0;
  }

  public boolean isHighRisk(double threshold) {
    return calculateOverallRisk() >= threshold;
  }

  public int getRewardPoints() {
    return rewardPoints;
  }

  public MembershipLevel getMembershipLevel() {
    return membershipLevel;
  }

  public void addRewardPoints(int basePoints) {
    if (basePoints < 0) throw new IllegalArgumentException("basePoints must be non-negative");
    int earned = (int) Math.round(basePoints * (1.0 - calculateOverallRisk()));
    rewardPoints += earned;
    updateMembershipLevel();
  }

  public boolean redeemRewardPoints(int points) {
    if (points < 0) throw new IllegalArgumentException("points must be non-negative");
    if (points > rewardPoints) return false;
    rewardPoints -= points;
    updateMembershipLevel();
    return true;
  }

  private void updateMembershipLevel() {
    if (rewardPoints >= 10_000) {
      membershipLevel = MembershipLevel.SILVER.PLATINUM;
    } else if (rewardPoints >= 5_000) {
      membershipLevel = MembershipLevel.SILVER.GOLD;
    } else if (rewardPoints >= 1_000) {
      membershipLevel = MembershipLevel.SILVER.SILVER;
    } else {
      membershipLevel = MembershipLevel.SILVER.BRONZE;
    }
  }
}