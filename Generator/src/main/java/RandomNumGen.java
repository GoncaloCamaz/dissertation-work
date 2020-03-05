import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomNumGen
{
    public int randomInt(int range)
    {
        Random rand = new Random();

        return 1 + rand.nextInt(range);
    }

    public double randomDouble(int range)
    {
        Random rand = new Random();
        int number = rand.nextInt(range);

        return number + rand.nextDouble();
    }

    public List<Integer> genNodesWServices(int maxNodes, int maxWServices)
    {
        List<Integer> range = new ArrayList<>();

        range = ThreadLocalRandom.current().ints(1, maxNodes).distinct().boxed().limit(maxWServices)
                                           .collect(Collectors.toCollection(ArrayList::new));
        Collections.sort(range);
        return range;
    }

    public int getRandomElement(List<Integer> numbers)
    {
        Collections.shuffle(numbers);
        return numbers.get(0);
    }
}
