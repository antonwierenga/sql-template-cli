# sql-template-cli
A command-line SQL tool whose main goal is to provide a quick way to execute predefined SQL statements (templates) against configured databases.
In addition it offers commands for listing tables, columns, data, Excel exports and more. All features are consistent to use across all supported databases.

![alt text](/demo.png "sql-template-cli screenshot")

## Installation
Download sql-template-cli in the [release section](https://github.com/antonwierenga/sql-template-cli/releases) of this repository.

Unzip the sql-template-cli-x.x.x.zip file and configure the databases you want to connect to in `sql-template-cli-x.x.x/conf/sql-template-cli.conf`:

```scala
database.db1.url="jdbc:postgresql://localhost:5432/db1"
database.db1.username=postgres

database.db2.url="jdbc:mysql://localhost:3307/db2"
database.db2.username=mysql
database.db2.password=my_plain_text_password

database.db3.url="jdbc:oracle:thin:@localhost:1521:db3"
database.db3.username=oracle
database.db3.password.encrypted="JPWORxwMO+k="

database.db4.url="jdbc:h2:~/db4"
database.db4.username=sa
database.db4.password=""
database.db4.template.dirs=[${app_home}/template/h2,${app_home}/template/my_app]
```
You can specify your own database aliases ('db1' .. 'db4' in above sample configuration).

Put the JDBC (version 4.0 or higher) drivers of the databases you want to connect to in the sql-template-cli-x.x.x/lib directory.

## Usage
To enter the sql-template-cli shell run `sql-template-cli-x.x.x/bin/sql-template-cli` or `sql-template-cli-x.x.x/bin/sql-template-cli.bat` if you are using Windows.

sql-template-cli provides tab completion to speed up typing commands, to see which commands are available and what parameters are supported.

In addition to typing commands in the shell it is also possible to pass a command directly to the sql-template-cli executable.

Example:`sql-template-cli tables --database db1`

It is also possible to specify a file containing commands to execute.

Example:`sql-template-cli --cmdfile my_commands.txt`

To connect to a database that is configured in `sql-template-cli-x.x.x/conf/sql-template-cli.conf` type in the following command in the shell: `connect --database db1`

Below is a list of commands that sql-template-cli supports. 

### connect
Connects to a database.

Technically speaking this command connects to the database to verify the database configuration and then disconnects. 
Every command runs using its own connection. The main reason to use the connect command is so the --database option can be omitted in subsequent commands. In addition when sql-template-cli is connected to a database it can provide auto-completion for the --table option in subsequent commands.

Note: table auto-completion depends on a template (located in sql-template-cli/template/provided/[DATABASE-TYPE]/tables.sql]) for the database type the command is executed for. If the template is not provided by sql-template-cli, you can create it yourself.

##### Parameters:
  - database

Example:`connect --database db1`

### columns

Lists the columns

Note: this command depends on a template (located in sql-template-cli/template/provided/[DATABASE-TYPE]/columns.sql]) for the database type this command is executed for. If the template is not provided by sql-template-cli, you can create it yourself.

##### Parameters:
  - table
  - database (if specified the command is executed against this database)

Example:`columns --table person`

### data

Lists records of a table (or view)

##### Parameters:
  - columns (comma seperated list of columns to show)
  - count (if specified only the count is returned)
  - database (if specified the command is executed against this database)
  - filter (conditions that the record must meet)
  - header-row (false to ommit the header row with the column names)
  - max-rows (max number of rows to show)
  - table-border (false to ommit the table border)
  - timeout (number of seconds to wait for the statement to execute)
  - order (the ordering of the records)
  - output-path (output path to use when output-format csv or excel is specified)  
  - output-format (console, csv or excel)

Example 1:`data --table person`

Example 2:`data --table person --filter surname='Doe' --order firstname`

Example 3:`data --table person --filter "firstname='John' and surname='Doe'" --count`

Example 4:`data --table person --order "surname, id desc" --output-format excel --output-path ~/Desktop`

### disconnect
Disconnects sql-template-cli from the database.

Example:`disconnect`

### encrypt-password

encrypts a password

Database passwords can be stored in plain text or encrypted in sql-template-cli.conf. Use this command to retrieve the encrypted value for a given password.

Store the password in plain text:
```scala
database.[DATABASE_ALIAS].password="secret" 
```

Store the password encrypted:
```scala
database.[DATABASE_ALIAS].password.encrypted="JPWORxwMO+k="
```

Please note that storing the password encrypted is not secure; it can easily be decrypted using the source code. It's always better to avoid storing passwords in sql-template-cli.conf and let sql-template-cli prompt for them instead.

Example:`encrypt-password`

### execute

Executes an inline SQL statement or template file containing the SQL statement

##### Parameters:
  - columns (comma seperated list of columns to show)
  - database (if specified the command is executed against this database)
  - header-row (false to ommit the header row with the column names)
  - max-rows (max number of rows to show)
  - sql (inline SQL statement to execute)
  - table-border (false to ommit the table border)
  - template (template file containing the SQL to execute (.sql extension may be omitted), supports TAB auto-completion)    
  - timeout (number of seconds to wait for the statement to execute)
  - output-path (output path to use when output-format csv or excel is specified)  
  - output-format (console, csv or excel)
  - p1 .. p20 (parameters required by the SQL statement)

Example 1:`execute --sql "select * from person"`

Example 2:`execute --template persons_by_city --p1 Amsterdam` 

The template must exist in either the `sql-template-cli-x.x.x/template` directory itself or in one of the configured template directories configured in `sql-template-cli-x.x.x/conf/sql-template-cli.conf`.

Given the following configuration:
```scala
database.db4.template.dirs=[${app_home}/template/h2,${app_home}/template/my_app]
```
sql-template-cli will look for templates that are executed for database with alias 'db4' in the following directories:

`sql-template-cli-x.x.x/template`

`sql-template-cli-x.x.x/template/h2`

`sql-template-cli-x.x.x/template/my_app`

### exit

Exits the sql-template-cli shell

Example:`exit`

### help 

List all commands usage

Example:`help`

### quit

Exits the sql-template-cli shell

Example:`quit`

### release-notes

Displays release notes

Example:`release-notes`

### script

Parses the specified resource file and executes its commands

##### Parameters:
  - file (the file containing the commands the execute)

Example:`script --file my_commands.txt`

### show-template

Shows the content of the specified template

##### Parameters:

  - name (the name of the template)

### tables

Lists tables

Note: this command depends on a template (sql-template-cli/template/provided/[DATABASE-TYPE]/tables.sql]) for the database type this command is executed for. If the template is not provided by sql-template-cli, you can create it yourself.

##### Parameters:

  - database (if specified the command is executed against this database)
  - filter (if specified only tables with the specified filter in their name are listed)

Example 1:`tables`

Example 2:`tables --filter person --database db1`

### version

Shows the database version

Note: this command depends on a template (sql-template-cli/template/provided/[DATABASE-TYPE]/version.sql]) for the database type this command is executed for. If the template is not provided by sql-template-cli, you can create it yourself.

##### Parameters:

Example:`version`

