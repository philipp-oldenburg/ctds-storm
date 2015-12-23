-- phpMyAdmin SQL Dump
-- version 4.2.7.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Dec 23, 2015 at 04:06 PM
-- Server version: 5.6.20
-- PHP Version: 5.5.15

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `ctds_db_test`
--
CREATE DATABASE IF NOT EXISTS `ctds_db_test` DEFAULT CHARACTER SET utf8 COLLATE utf8_bin;
USE `ctds_db_test`;

-- --------------------------------------------------------

--
-- Table structure for table `weatherdatalog`
--

CREATE TABLE IF NOT EXISTS `weatherdatalog` (
`id` int(11) NOT NULL,
  `timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `temperature` double DEFAULT NULL,
  `pressure` double DEFAULT NULL,
  `humidity` double DEFAULT NULL,
  `sensorwindspeed` double DEFAULT NULL,
  `owmwindspeed` double DEFAULT NULL,
  `owmwinddegree` double DEFAULT NULL,
  `light` double DEFAULT NULL,
  `owmweathername` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `owmweatherdesc` varchar(100) COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=45 ;

--
-- Dumping data for table `weatherdatalog`
--

INSERT INTO `weatherdatalog` (`id`, `timestamp`, `temperature`, `pressure`, `humidity`, `sensorwindspeed`, `owmwindspeed`, `owmwinddegree`, `light`, `owmweathername`, `owmweatherdesc`) VALUES
(1, '2015-12-23 15:30:08', 11.222222646077475, 1030, 76, -1, 4.539999961853027, 270, -1, 'Clouds', 'broken clouds'),
(2, '2015-12-23 15:30:20', 11.222222646077475, 1030, 76, -1, 4.539999961853027, 270, -1, 'Clouds', 'broken clouds'),
(3, '2015-12-23 15:32:45', 13.07312, 99752, 93.505859, -1, 4.539999961853027, 270, 2986, 'Clouds', 'broken clouds'),
(4, '2015-12-23 15:32:56', 13.063049, 99737, 93.457031, -1, 4.539999961853027, 270, 2986, 'Clouds', 'broken clouds'),
(5, '2015-12-23 15:33:07', 13.042908, 99739, 93.505859, -1, 4.539999961853027, 270, 2978, 'Clouds', 'broken clouds'),
(6, '2015-12-23 15:33:21', 13.002625, 99737, 92.932129, -1, 4.539999961853027, 270, 2966, 'Clouds', 'broken clouds'),
(7, '2015-12-23 15:33:33', 13.002625, 99750, 92.932129, -1, 4.539999961853027, 270, 2956, 'Clouds', 'broken clouds'),
(8, '2015-12-23 15:33:44', 13.002625, 99724, 93.695068, -1, 4.539999961853027, 270, 2946, 'Clouds', 'broken clouds'),
(9, '2015-12-23 15:33:55', 12.972412, 99741, 94.268799, -1, 4.539999961853027, 270, 2934, 'Clouds', 'broken clouds'),
(10, '2015-12-23 15:34:07', 12.962341, 99752, 94.268799, -1, 4.539999961853027, 270, 2915, 'Clouds', 'broken clouds'),
(11, '2015-12-23 15:36:12', 12.881775, 997.32, 96.368408, -1, 4.539999961853027, 270, 2633, 'Clouds', 'broken clouds'),
(12, '2015-12-23 15:36:23', 12.972412, 997.38, 95.88623, -1, 4.539999961853027, 270, 2609, 'Clouds', 'broken clouds'),
(13, '2015-12-23 15:36:35', 12.992554, 997.41, 95.605469, -1, 4.539999961853027, 270, 2586, 'Clouds', 'broken clouds'),
(14, '2015-12-23 15:36:46', 12.972412, 997.43, 95.605469, -1, 4.539999961853027, 270, 2568, 'Clouds', 'broken clouds'),
(15, '2015-12-23 15:36:58', 12.972412, 997.4, 95.507812, -1, 4.539999961853027, 270, 2548, 'Clouds', 'broken clouds'),
(16, '2015-12-23 15:37:09', 13.042908, 997.46, 95.410156, -1, 4.539999961853027, 270, 2527, 'Clouds', 'broken clouds'),
(17, '2015-12-23 15:37:20', 13.032837, 997.38, 95.220947, -1, 4.539999961853027, 270, 2504, 'Clouds', 'broken clouds'),
(18, '2015-12-23 15:37:32', 12.992554, 997.34, 95.507812, -1, 4.539999961853027, 270, 2480, 'Clouds', 'broken clouds'),
(19, '2015-12-23 15:37:43', 13.002625, 997.43, 95.605469, -1, 4.539999961853027, 270, 2455, 'Clouds', 'broken clouds'),
(20, '2015-12-23 15:37:55', 13.002625, 997.38, 95.220947, -1, 4.539999961853027, 270, 2425, 'Clouds', 'broken clouds'),
(21, '2015-12-23 15:38:06', 13.022766, 997.35, 95.318604, -1, 4.539999961853027, 270, 2396, 'Clouds', 'broken clouds'),
(22, '2015-12-23 15:38:21', 12.992554, 997.4, 95.220947, -1, 4.539999961853027, 270, 2358, 'Clouds', 'broken clouds'),
(23, '2015-12-23 15:38:32', 12.972412, 997.46, 95.410156, -1, 4.539999961853027, 270, 2328, 'Clouds', 'broken clouds'),
(24, '2015-12-23 15:38:43', 12.992554, 997.41, 95.318604, -1, 4.539999961853027, 270, 2298, 'Clouds', 'broken clouds'),
(25, '2015-12-23 15:38:54', 13.002625, 997.4, 95.318604, -1, 4.539999961853027, 270, 2272, 'Clouds', 'broken clouds'),
(26, '2015-12-23 15:39:05', 12.972412, 997.35, 95.220947, -1, 4.539999961853027, 270, 2246, 'Clouds', 'broken clouds'),
(27, '2015-12-23 15:39:20', 12.9422, 997.56, 95.697021, -1, 4.539999961853027, 270, 2211, 'Clouds', 'broken clouds'),
(28, '2015-12-23 15:39:31', 12.922058, 997.46, 95.697021, -1, 4.539999961853027, 270, 2180, 'Clouds', 'broken clouds'),
(29, '2015-12-23 15:39:42', 12.972412, 997.37, 95.794678, -1, 4.539999961853027, 270, 2146, 'Clouds', 'broken clouds'),
(30, '2015-12-23 15:39:54', 12.992554, 997.38, 95.983887, -1, 4.539999961853027, 270, 2108, 'Clouds', 'broken clouds'),
(31, '2015-12-23 15:40:05', 12.992554, 997.38, 95.794678, -1, 4.539999961853027, 270, 2068, 'Clouds', 'broken clouds'),
(32, '2015-12-23 15:40:20', 12.911987, 997.53, 95.88623, -1, 4.539999961853027, 270, 2021, 'Clouds', 'broken clouds'),
(33, '2015-12-23 15:40:32', 12.972412, 997.44, 95.88623, -1, 4.539999961853027, 270, 1991, 'Clouds', 'broken clouds'),
(34, '2015-12-23 15:40:42', 13.032837, 997.44, 95.794678, -1, 4.539999961853027, 270, 1963, 'Clouds', 'broken clouds'),
(35, '2015-12-23 15:40:55', 13.063049, 997.46, 95.507812, -1, 4.539999961853027, 270, 1933, 'Clouds', 'broken clouds'),
(36, '2015-12-23 15:41:06', 13.032837, 997.5, 95.697021, -1, 4.539999961853027, 270, 1905, 'Clouds', 'broken clouds'),
(37, '2015-12-23 15:41:21', 13.032837, 997.37, 95.88623, -1, 4.539999961853027, 270, 1869, 'Clouds', 'broken clouds'),
(38, '2015-12-23 15:41:32', 12.962341, 997.44, 95.88623, -1, 4.539999961853027, 270, 1841, 'Clouds', 'broken clouds'),
(39, '2015-12-23 15:41:43', 12.972412, 997.46, 95.697021, -1, 4.539999961853027, 270, 1813, 'Clouds', 'broken clouds'),
(40, '2015-12-23 15:41:54', 12.972412, 997.46, 95.983887, -1, 4.539999961853027, 270, 1786, 'Clouds', 'broken clouds'),
(41, '2015-12-23 15:42:06', 12.962341, 997.47, 96.173096, -1, 4.539999961853027, 270, 1759, 'Clouds', 'broken clouds'),
(42, '2015-12-23 15:42:21', 12.972412, 997.44, 96.270752, -1, 4.539999961853027, 270, 1726, 'Clouds', 'broken clouds'),
(43, '2015-12-23 15:42:32', 12.962341, 997.5, 96.368408, -1, 4.539999961853027, 270, 1700, 'Clouds', 'broken clouds'),
(44, '2015-12-23 15:43:00', 12.972412, 997.5, 96.368408, -1, 4.539999961853027, 270, 1643, 'Clouds', 'broken clouds');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `weatherdatalog`
--
ALTER TABLE `weatherdatalog`
 ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `weatherdatalog`
--
ALTER TABLE `weatherdatalog`
MODIFY `id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=45;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
