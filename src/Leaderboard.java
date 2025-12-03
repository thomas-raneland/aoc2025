import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;

public class Leaderboard {
    public static void main(String... args) {
        String json = AocUtils.downloadLeaderboard();
        Board board = new Gson().fromJson(json, Board.class);
        Map<String, Map<Star, Instant>> memberStars = new TreeMap<>();

        board.members.forEach((memberId, member) ->
                member.completion_day_level.forEach((dayAsString, parts) ->
                        parts.forEach((partAsString, part) -> {
                            Instant time = Instant.ofEpochSecond(part.get_star_ts);
                            Star star = new Star(Integer.parseInt(dayAsString), Integer.parseInt(partAsString));
                            memberStars.computeIfAbsent(member.name, x -> new TreeMap<>()).put(star, time);
                        })
                )
        );

        memberStars.forEach((name, stars) ->
                stars.forEach((star, endTime) -> {
                    Instant startTime = star.part() == 2 ? stars.get(star.part1()) : star.released();
                    Duration duration = Duration.between(startTime, endTime);
                    System.out.println(name + " " + star + ": " + duration.toMinutes());
                })
        );
    }

    private record Star(int day, int part) implements Comparable<Star> {
        @Override
        public int compareTo(Star o) {
            return Comparator.comparingInt(Star::day).thenComparingInt(Star::part).compare(this, o);
        }

        @Override
        public String toString() {
            return new DecimalFormat("00").format(day) + ":" + part;
        }

        Star part1() {
            return new Star(day, 1);
        }

        Instant released() {
            return LocalDateTime.of(2025, Month.DECEMBER, day, 6, 0).atZone(ZoneId.systemDefault()).toInstant();
        }
    }

    private record Board(
            String event,
            long day1_ts,
            Map<String, Member> members,
            long owner_id) {}

    private record Member(
            int global_score,
            int id,
            long last_star_ts,
            Map<String, Map<String, Part>> completion_day_level,
            String name,
            int local_score,
            int stars) {}

    private record Part(
            long get_star_ts,
            long star_index) {}
}
