package com.knstech.friendsapp2;

public class Messages {

        private  String message,type,from;

        public String getFrom() {
                return from;
        }

        public void setFrom(String from) {
                this.from = from;
        }

        private boolean seen;
        private  Object time;

        public Messages() {
        }

        public Messages(String message, String type, boolean seen, Object time,String from) {

                this.message = message;
                this.type = type;
                this.seen = seen;
                this.time = time;
                this.from=from;
        }

        public String getMessage() {

                return message;
        }

        public void setMessage(String message) {
                this.message = message;
        }

        public String getType() {
                return type;
        }

        public void setType(String type) {
                this.type = type;
        }

        public boolean isSeen() {
                return seen;
        }

        public void setSeen(boolean seen) {
                this.seen = seen;
        }

        public Object getTime() {
                return time;
        }

        public void setTime(Object time) {
                this.time = time;
        }
}