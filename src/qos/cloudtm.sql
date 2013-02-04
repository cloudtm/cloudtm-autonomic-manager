
SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `cloudtm`
--

-- --------------------------------------------------------

--
-- Struttura della tabella `application`
--

CREATE TABLE IF NOT EXISTS `application` (
  `id_application` int(10) NOT NULL AUTO_INCREMENT,
  `id_user` int(10) NOT NULL,
  `id_template` int(10) NOT NULL,
  `appname` varchar(120) NOT NULL,
  `id_app_status` int(10) NOT NULL,
  `uploadDate` date NOT NULL,
  PRIMARY KEY (`id_application`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=9 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `app_status`
--

CREATE TABLE IF NOT EXISTS `app_status` (
  `id_app_status` int(10) NOT NULL,
  `name` varchar(128) NOT NULL,
  PRIMARY KEY (`id_app_status`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `attribute`
--

CREATE TABLE IF NOT EXISTS `attribute` (
  `id_attribute` int(10) NOT NULL,
  `name` varchar(128) NOT NULL,
  PRIMARY KEY (`id_attribute`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `chart`
--

CREATE TABLE IF NOT EXISTS `chart` (
  `id_chart` int(10) NOT NULL AUTO_INCREMENT,
  `id_attribute` int(10) NOT NULL,
  `id_sla_prediction` int(10) NOT NULL,
  `id_transactional_class` int(10) NOT NULL,
  PRIMARY KEY (`id_chart`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=439 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `chart_value`
--

CREATE TABLE IF NOT EXISTS `chart_value` (
  `id_chart` int(10) NOT NULL,
  `x_value` double NOT NULL,
  `y_value` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `pending_sla`
--

CREATE TABLE IF NOT EXISTS `pending_sla` (
  `id_pending_sla` int(10) NOT NULL AUTO_INCREMENT,
  `id_template` int(10) NOT NULL,
  `creationDate` datetime NOT NULL,
  PRIMARY KEY (`id_pending_sla`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=80 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `pending_sla_detail`
--

CREATE TABLE IF NOT EXISTS `pending_sla_detail` (
  `id_pending_sla` int(10) NOT NULL,
  `id_sla_prediction` int(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `rhq_config`
--

CREATE TABLE IF NOT EXISTS `rhq_config` (
  `id_rhq_config` int(10) NOT NULL AUTO_INCREMENT,
  `id_sla_prediction` int(10) NOT NULL,
  PRIMARY KEY (`id_rhq_config`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=37 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `rhq_config_resource`
--

CREATE TABLE IF NOT EXISTS `rhq_config_resource` (
  `id_rhq_config` int(10) NOT NULL,
  `id_sla_chart_group` int(10) NOT NULL,
  `resource_name` varchar(128) NOT NULL,
  PRIMARY KEY (`id_rhq_config`,`id_sla_chart_group`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `sla_chart`
--

CREATE TABLE IF NOT EXISTS `sla_chart` (
  `id_sla_prediction` int(10) NOT NULL,
  `id_chart` int(10) NOT NULL,
  `id_sla_chart_group` int(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `sla_chart_group`
--

CREATE TABLE IF NOT EXISTS `sla_chart_group` (
  `id_sla_chart_group` int(10) NOT NULL,
  `name` varchar(128) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `sla_prediction`
--

CREATE TABLE IF NOT EXISTS `sla_prediction` (
  `id_sla_prediction` int(10) NOT NULL AUTO_INCREMENT,
  `id_workload_characterization` int(10) NOT NULL,
  `creationDate` datetime NOT NULL,
  `id_template_status` int(10) NOT NULL,
  PRIMARY KEY (`id_sla_prediction`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=119 ;

-- --------------------------------------------------------



--
-- Struttura della tabella `template`
--

CREATE TABLE IF NOT EXISTS `template` (
  `id_template` int(10) NOT NULL AUTO_INCREMENT,
  `id_user` int(10) NOT NULL,
  `creationDate` datetime NOT NULL,
  `id_template_status` int(10) NOT NULL,
  PRIMARY KEY (`id_template`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=80 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `template_status`
--

CREATE TABLE IF NOT EXISTS `template_status` (
  `id_template_status` int(10) NOT NULL,
  `name` varchar(128) NOT NULL,
  PRIMARY KEY (`id_template_status`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `transactional_class`
--

CREATE TABLE IF NOT EXISTS `transactional_class` (
  `id_transactional_class` int(10) NOT NULL AUTO_INCREMENT,
  `id_template` int(10) NOT NULL,
  `name` varchar(128) NOT NULL,
  `throughput` double NOT NULL,
  `response_time` double NOT NULL,
  `response_time_percentile` double NOT NULL,
  `abort_rate` double NOT NULL,
  `period` double NOT NULL,
  PRIMARY KEY (`id_transactional_class`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=40 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `id_user` int(10) NOT NULL AUTO_INCREMENT,
  `FirstName` varchar(32) NOT NULL,
  `LastName` varchar(32) NOT NULL,
  `user_name` varchar(32) NOT NULL,
  `password` varchar(32) NOT NULL,
  `email` varchar(64) NOT NULL,
  `level` int(1) NOT NULL,
  `registrationDate` date NOT NULL,
  PRIMARY KEY (`id_user`),
  KEY `user_login_key` (`user_name`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=7 ;

INSERT INTO `users` (`id_user`, `FirstName`, `LastName`, `user_name`, `password`, `email`, `level`, `registrationDate`) VALUES
(1, 'admin', 'admin', 'admin', 'admin', '', 0, '0000-00-00');


--
-- Dump dei dati per la tabella `sla_chart_group`
--

INSERT INTO `sla_chart_group` (`id_sla_chart_group`, `name`) VALUES
(1, 'Costs'),
(2, 'Abort Rate'),
(3, 'Response Time');

-- --------------------------------------------------------

--
-- Dump dei dati per la tabella `template_status`
--

INSERT INTO `template_status` (`id_template_status`, `name`) VALUES
(1, 'Accepted'),
(2, 'Rejected');

-- --------------------------------------------------------
--
-- Dump dei dati per la tabella `app_status`
--

INSERT INTO `app_status` (`id_app_status`, `name`) VALUES
(1, 'Waiting for deploy'),
(2, 'Deployed / Waiting for predictions'),
(3, 'Predictions Available'),
(4, 'Accepted'),
(5, 'Rejected');

-- --------------------------------------------------------

--
-- Dump dei dati per la tabella `attribute`
--

INSERT INTO `attribute` (`id_attribute`, `name`) VALUES
(0, 'Nodes'),
(1, 'Threads'),
(2, 'Abort Rate'),
(3, 'Response Time');

-- --------------------------------------------------------
