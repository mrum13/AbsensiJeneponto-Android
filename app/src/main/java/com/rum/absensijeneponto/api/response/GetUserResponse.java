package com.rum.absensijeneponto.api.response;

import com.google.gson.annotations.SerializedName;

public class GetUserResponse {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("email")
    private String email;
    @SerializedName("email_verified_at")
    private String emailVerifiedAt;
    @SerializedName("role")
    private String role;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;
    @SerializedName("pegawai")
    private Pegawai pegawai;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(String emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Pegawai getPegawai() {
        return pegawai;
    }

    public void setPegawai(Pegawai pegawai) {
        this.pegawai = pegawai;
    }

    public class Pegawai {
        @SerializedName("id")
        private int id;
        @SerializedName("user_id")
        private String userId;
        @SerializedName("nip")
        private String nip;
        @SerializedName("tempat_lahir")
        private String tempatLahir;
        @SerializedName("tanggal_lahir")
        private String tanggalLahir;
        @SerializedName("lokasi_id")
        private String lokasiId;
        @SerializedName("golongan_id")
        private String golonganId;
        @SerializedName("jenis_kelamin")
        private String jenisKelamin;
        @SerializedName("agama")
        private String agama;
        @SerializedName("no_hp")
        private String no_hp;
        @SerializedName("face_character")
        private String face_character;
        @SerializedName("created_at")
        private String createdAt;
        @SerializedName("updated_at")
        private String updatedAt;
        @SerializedName("lokasi")
        private Lokasi lokasi;
        @SerializedName("golongan")
        private Golongan golongan;

        public int getId() {
            return id;
        }

        public String getUserId() {
            return userId;
        }

        public String getNip() {
            return nip;
        }

        public String getTempatLahir() {
            return tempatLahir;
        }

        public String getTanggalLahir() {
            return tanggalLahir;
        }

        public String getLokasiId() {
            return lokasiId;
        }

        public String getGolonganId() {
            return golonganId;
        }

        public String getJenisKelamin() {
            return jenisKelamin;
        }

        public String getAgama() {
            return agama;
        }

        public String getNo_hp() {
            return no_hp;
        }

        public String getFace_character() {
            return face_character;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public Lokasi getLokasi() {
            return lokasi;
        }

        public Golongan getGolongan() {
            return golongan;
        }

        public class Lokasi {
            @SerializedName("id")
            private int id;
            @SerializedName("kode")
            private String kode;
            @SerializedName("nama")
            private String nama;
            @SerializedName("lat_location")
            private String latLocation;
            @SerializedName("long_location")
            private String longLocation;
            @SerializedName("created_at")
            private String createdAt;
            @SerializedName("updated_at")
            private String updatedAt;

            public int getId() {
                return id;
            }

            public String getKode() {
                return kode;
            }

            public String getNama() {
                return nama;
            }

            public String getLatLocation() {
                return latLocation;
            }

            public String getLongLocation() {
                return longLocation;
            }

            public String getCreatedAt() {
                return createdAt;
            }

            public String getUpdatedAt() {
                return updatedAt;
            }
        }

        public class Golongan  {
            @SerializedName("id")
            private int id;
            @SerializedName("golongan")
            private String golongan;
            @SerializedName("pangkat")
            private String pangkat;
            @SerializedName("tpp")
            private String tpp;
            @SerializedName("created_at")
            private String createdAt;
            @SerializedName("updated_at")
            private String updatedAt;

            public int getId() {
                return id;
            }

            public String getGolongan() {
                return golongan;
            }

            public String getPangkat() {
                return pangkat;
            }

            public String getTpp() {
                return tpp;
            }

            public String getCreatedAt() {
                return createdAt;
            }

            public String getUpdatedAt() {
                return updatedAt;
            }
        }
    }
}

