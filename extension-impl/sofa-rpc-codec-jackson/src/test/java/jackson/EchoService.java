package jackson;

/**
 * @author 景竹 2018/7/29
 */
public interface EchoService {

    /**
     * Check out int. @param echo the echo
     *
     * @param name the name
     * @return the int
     */
    int checkOut(Echo echo,String name);
}
