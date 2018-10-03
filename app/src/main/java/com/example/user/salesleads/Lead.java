package com.example.user.salesleads;

import java.util.ArrayList;

public class Lead {
        private String leadName;
        private String product;
        private String date;
        private String comment;

        public Lead(){

        }
        public Lead(String leadName, String product, String date) {
            this.leadName = leadName;
            this.product = product;
            this.date = date;
        }


        public String getLeadName() {
            return leadName;
        }

        public void setLeadName(String leadName) {
            this.leadName = leadName;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
}
