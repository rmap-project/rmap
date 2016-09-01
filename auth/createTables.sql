CREATE TABLE `ApiKeys` (
  `apiKeyId` int(11) NOT NULL AUTO_INCREMENT,
  `accessKey` varchar(256) DEFAULT NULL,
  `secret` varchar(1024) DEFAULT NULL,
  `keyUri` varchar(128) DEFAULT NULL,
  `label` varchar(128) DEFAULT NULL,
  `note` varchar(1024) DEFAULT NULL,
  `keyStatus` varchar(32) DEFAULT NULL,
  `includeInEvent` tinyint(1) DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `createdDate` datetime DEFAULT NULL,
  `lastModifiedDate` datetime DEFAULT NULL,
  `revokedDate` datetime DEFAULT NULL,
  `userId` int(11) DEFAULT NULL,
  PRIMARY KEY (`apiKeyId`),
  UNIQUE KEY `apiKeyId_UNIQUE` (`apiKeyId`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;

CREATE TABLE `UserIdentityProviders` (
  `userIdentityProviderId` int(11) NOT NULL AUTO_INCREMENT,
  `identityProvider` varchar(128) DEFAULT NULL,
  `providerAccountId` varchar(256) DEFAULT NULL,
  `providerAccountPublicId` varchar(45) DEFAULT NULL,
  `providerAccountDisplayName` varchar(45) DEFAULT NULL,
  `providerAccountProfileUrl` varchar(512) DEFAULT NULL,
  `lastAuthenticatedDate` datetime DEFAULT NULL,
  `createdDate` datetime DEFAULT NULL,
  `userId` int(11) NOT NULL,
  PRIMARY KEY (`userIdentityProviderId`),
  UNIQUE KEY `userIdentityProviderId_UNIQUE` (`userIdentityProviderId`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;

CREATE TABLE `Users` (
  `userId` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) NOT NULL,
  `email` varchar(128) NOT NULL,
  `isActive` tinyint(1) unsigned zerofill NOT NULL,
  `rmapAgentUri` varchar(128) DEFAULT NULL,
  `authKeyUri` varchar(256) DEFAULT NULL,
  `createdDate` datetime NOT NULL,
  `lastAccessedDate` datetime DEFAULT NULL,
  `cancellationDate` datetime DEFAULT NULL,
  `rmapDiSCOUri` varchar(45) DEFAULT NULL,
  `doRMapAgentSync` tinyint(1) unsigned zerofill NOT NULL,
  PRIMARY KEY (`userId`),
  UNIQUE KEY `userId_UNIQUE` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=latin1;