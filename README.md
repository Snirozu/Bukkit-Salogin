# Salogin
Spigot plugin for authentication purposes which uses Password4J for hashing <br>
###### *most of this code was written in 2022, don't expect something really good (it's better than authme still)*

## Features
* Basic `/login` and `/register` commands with login alias "`/l`"
* E-Mail verification for people that forgor their password
* Logout position masking
* SQLite and MySQL support

## FAQ
### Can i migrate the database from other plugin?
Because hashing can't be reversed back to string, you can't migrate database from other plugin to this plugin's database. Sorry! <br>
The only possible solution would be to get the same way to hash string from other plugin, to which i'm too lazy to make.
<!-- ### How can i change player's password?
For privacy reasons and shit you cannot set someone's password, but you can generate a one-time login code for player with: <br>
`/saloginadmin generateLoginCode {player}` -->