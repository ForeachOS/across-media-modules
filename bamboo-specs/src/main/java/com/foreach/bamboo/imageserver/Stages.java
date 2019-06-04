package com.foreach.bamboo.imageserver;

import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.plan.Job;
import com.atlassian.bamboo.specs.api.builders.plan.Stage;
import com.atlassian.bamboo.specs.api.builders.plan.configuration.AllOtherPluginsConfiguration;
import com.atlassian.bamboo.specs.builders.task.ScpTask;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.builders.task.SshTask;
import com.atlassian.bamboo.specs.builders.task.TestParserTask;
import com.atlassian.bamboo.specs.model.task.TestParserTaskProperties;

import static com.foreach.bamboo.imageserver.Jobs.*;

public class Stages
{
	public static Stage unitTests() {
		return new Stage( "Default Stage" )
				.jobs( new Job( "Run unit tests",
				                new BambooKey( "JOB1" ) )
						       .pluginConfigurations( new AllOtherPluginsConfiguration() )
						       .tasks(
								       defaultRepositoryCheckoutTask(),
								       bambooSpecsRepositoryCheckoutTask(),
								       new ScriptTask()
										       .description( "Run unit tests" )
										       .inlineBody( "docker-compose --no-ansi run --rm maven-gm mvn --batch-mode -U clean verify" ),
								       new TestParserTask( TestParserTaskProperties.TestType.JUNIT )
										       .description( "Parse junit reports" )
										       .resultDirectories( "**/target/surefire-reports/*.xml" )
						       )
						       .requirements( requiresLinux() ) );
	}

	public static Stage integrationTests() {
		return new Stage( "Run multi-database tests" )
				.jobs(
						crossDbTest( "H2", "ITH2", "mysql" ),
						crossDbTest( "MySQL", "ITMYSQL", "mysql" ),
						crossDbTest( "Oracle", "ITOR", "oracle" ),
						crossDbTest( "Postgres", "ITPOS", "postgres" ),
						crossDbTest( "SQL Server", "ITMSSQL", "mssql" )
				);
	}

	public static Stage deploySnapshot() {
		return new Stage( "Deploy snapshot" )
				.manual( true )
				.jobs( new Job( "Deploy snapshot",
				                new BambooKey( "DS" ) )
						       .tasks(
								       defaultRepositoryCheckoutTask(),
								       bambooSpecsRepositoryCheckoutTask(),
								       new ScriptTask()
										       .inlineBody(
												       "docker-compose --no-ansi run --rm maven-gm mvn -U --batch-mode clean compile assembly:assembly deploy -DskipTests=true -Pforeach -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true" ),
								       new ScpTask()
										       .description( "Upload documentation" )
										       .host( "192.168.2.42" )
										       .username( "buildserver" )
										       .toRemotePath( "/var/www/repository.foreach.be/projects/image-server" )
										       .authenticateWithKeyWithPassphrase(
												       "BAMSCRT@0@0@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",
												       "BAMSCRT@0@0@cwhCpmAjUU5zzI6BDxxa8w==" )
										       .fromLocalPath( "target/*.tar", true ),
								       new SshTask().authenticateWithKey(
										       "BAMSCRT@0@0@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" )
								                    .description( "Unpack documentation" )
								                    .host( "192.168.2.42" )
								                    .username( "buildserver" )
								                    .command(
										                    "cd /var/www/repository.foreach.be/projects/image-server\r\ntar -xvf imageserver-platform-distribution.tar\r\nrm imageserver-platform-distribution.tar" )
						       )
						       .requirements( requiresLinux() ) );
	}
}
