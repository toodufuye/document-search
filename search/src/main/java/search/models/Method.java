package search.models;


public enum Method {
        StringMatch(1),
        RegexMatch(2),
        Indexed(3);

        public final int num;

        Method(int num) {
            this.num = num;
        }

        public static Method getEndpoint(int num) {
            Method temp;
            switch (num) {
                case 1:
                    temp = Method.StringMatch;
                    break;
                case 2:
                    temp = Method.RegexMatch;
                    break;
                case 3:
                    temp = Method.Indexed;
                    break;
                default:
                    temp = Method.StringMatch;
            }
            return temp;
        }
    }
